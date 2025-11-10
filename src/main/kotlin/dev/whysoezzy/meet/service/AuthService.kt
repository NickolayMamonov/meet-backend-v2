package dev.whysoezzy.meet.service

import dev.whysoezzy.meet.api.dto.auth.*
import dev.whysoezzy.meet.common.exception.BadRequestException
import dev.whysoezzy.meet.common.exception.ConflictException
import dev.whysoezzy.meet.common.exception.ResourceNotFoundException
import dev.whysoezzy.meet.common.exception.TooManyRequestsException
import dev.whysoezzy.meet.config.JwtTokenProvider
import dev.whysoezzy.meet.domain.entity.SmsCode
import dev.whysoezzy.meet.domain.entity.User
import dev.whysoezzy.meet.domain.repository.SmsCodeRepository
import dev.whysoezzy.meet.domain.repository.TagRepository
import dev.whysoezzy.meet.domain.repository.UserRepository
import mu.KotlinLogging
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import kotlin.random.Random

private val logger = KotlinLogging.logger {}

@Service
class AuthService(
    private val userRepository: UserRepository,
    private val smsCodeRepository: SmsCodeRepository,
    private val tagRepository: TagRepository,
    private val smsService: SmsService,
    private val jwtTokenProvider: JwtTokenProvider
) {
    
    @Transactional
    fun sendVerificationCode(phoneNumber: String) {
        logger.info { "Sending verification code to: $phoneNumber" }
        
        // Check rate limiting
        val existingCode = smsCodeRepository.findByPhoneNumber(phoneNumber)
        if (existingCode != null && !existingCode.isExpired()) {
            val timeSinceCreation = Instant.now().epochSecond - existingCode.createdAt.epochSecond
            if (timeSinceCreation < 60) {
                throw TooManyRequestsException("Please wait before requesting a new code")
            }
        }
        
        // Generate 6-digit code
        val code = Random.nextInt(100000, 999999).toString()
        
        // Save or update SMS code
        val smsCode = existingCode?.apply {
            this.code = code
            this.expiresAt = Instant.now().plusSeconds(600) // 10 minutes
            this.attempts = 0
            this.createdAt = Instant.now()
        } ?: SmsCode(
            phoneNumber = phoneNumber,
            code = code,
            expiresAt = Instant.now().plusSeconds(600),
            attempts = 0
        )
        
        smsCodeRepository.save(smsCode)
        
        // Send SMS
        try {
            smsService.sendVerificationCode(phoneNumber, code)
            logger.info { "Verification code sent successfully to: $phoneNumber" }
        } catch (e: Exception) {
            logger.error(e) { "Failed to send SMS to: $phoneNumber" }
            throw BadRequestException("Failed to send verification code")
        }
    }
    
    @Transactional
    fun verifyCode(phoneNumber: String, code: String): VerifyCodeResponse {
        logger.info { "Verifying code for: $phoneNumber" }
        
        val smsCode = smsCodeRepository.findByPhoneNumber(phoneNumber)
            ?: throw BadRequestException("No verification code found. Please request a new one.")
        
        if (smsCode.isExpired()) {
            smsCodeRepository.delete(smsCode)
            throw BadRequestException("Verification code expired. Please request a new one.")
        }
        
        if (smsCode.hasExceededAttempts()) {
            throw TooManyRequestsException("Too many failed attempts. Please request a new code.")
        }
        
        if (smsCode.code != code) {
            smsCode.incrementAttempts()
            smsCodeRepository.save(smsCode)
            throw BadRequestException("Invalid verification code")
        }
        
        // Code is valid - delete it
        smsCodeRepository.delete(smsCode)
        
        // Check if user exists
        val existingUser = userRepository.findByPhoneNumber(phoneNumber)
        val isNewUser = existingUser == null
        
        // Generate temporary token for registration or login
        val temporaryToken = jwtTokenProvider.generateTemporaryToken(phoneNumber)
        
        logger.info { "Code verified successfully for: $phoneNumber, isNewUser: $isNewUser" }
        
        return VerifyCodeResponse(
            temporaryToken = temporaryToken,
            isNewUser = isNewUser
        )
    }
    
    @Transactional
    fun completeRegistration(
        temporaryToken: String,
        name: String,
        surname: String,
        interests: List<Long>?
    ): LoginResponse {
        logger.info { "Completing registration with temporary token" }
        
        // Validate temporary token
        if (!jwtTokenProvider.validateToken(temporaryToken)) {
            throw BadRequestException("Invalid or expired token")
        }
        
        val phoneNumber = jwtTokenProvider.getPhoneNumberFromTemporaryToken(temporaryToken)
            ?: throw BadRequestException("Invalid token")
        
        // Check if user already exists
        if (userRepository.existsByPhoneNumber(phoneNumber)) {
            throw ConflictException("User already exists")
        }
        
        // Create new user
        val user = User(
            name = name,
            surname = surname,
            phoneNumber = phoneNumber
        )
        
        // Add interests if provided
        if (!interests.isNullOrEmpty()) {
            val tags = tagRepository.findAllById(interests)
            user.interests.addAll(tags)
        }
        
        val savedUser = userRepository.save(user)
        
        logger.info { "User registered successfully: ${savedUser.id}" }
        
        // Generate tokens
        val accessToken = jwtTokenProvider.generateAccessToken(savedUser.id!!)
        val refreshToken = jwtTokenProvider.generateRefreshToken(savedUser.id!!)
        
        return LoginResponse(
            accessToken = accessToken,
            refreshToken = refreshToken,
            user = savedUser.toAuthDto()
        )
    }
    
    @Transactional(readOnly = true)
    fun login(temporaryToken: String): LoginResponse {
        logger.info { "Login with temporary token" }
        
        // Validate temporary token
        if (!jwtTokenProvider.validateToken(temporaryToken)) {
            throw BadRequestException("Invalid or expired token")
        }
        
        val phoneNumber = jwtTokenProvider.getPhoneNumberFromTemporaryToken(temporaryToken)
            ?: throw BadRequestException("Invalid token")
        
        val user = userRepository.findByPhoneNumber(phoneNumber)
            ?: throw ResourceNotFoundException("User not found")
        
        logger.info { "User logged in successfully: ${user.id}" }
        
        // Generate tokens
        val accessToken = jwtTokenProvider.generateAccessToken(user.id!!)
        val refreshToken = jwtTokenProvider.generateRefreshToken(user.id!!)
        
        return LoginResponse(
            accessToken = accessToken,
            refreshToken = refreshToken,
            user = user.toAuthDto()
        )
    }
    
    fun refreshToken(refreshToken: String): RefreshTokenResponse {
        logger.info { "Refreshing token" }
        
        if (!jwtTokenProvider.validateToken(refreshToken)) {
            throw BadRequestException("Invalid or expired refresh token")
        }
        
        if (!jwtTokenProvider.isRefreshToken(refreshToken)) {
            throw BadRequestException("Invalid token type")
        }
        
        val userId = jwtTokenProvider.getUserIdFromToken(refreshToken)
            ?: throw BadRequestException("Invalid token")
        
        // Verify user still exists
        if (!userRepository.existsById(userId)) {
            throw ResourceNotFoundException("User not found")
        }
        
        // Generate new tokens
        val newAccessToken = jwtTokenProvider.generateAccessToken(userId)
        val newRefreshToken = jwtTokenProvider.generateRefreshToken(userId)
        
        logger.info { "Token refreshed successfully for user: $userId" }
        
        return RefreshTokenResponse(
            accessToken = newAccessToken,
            refreshToken = newRefreshToken
        )
    }
    
    private fun User.toAuthDto() = UserAuthDto(
        id = id!!,
        name = name,
        surname = surname,
        phoneNumber = phoneNumber,
        imageUrl = imageUrl,
        city = city,
        description = description
    )
}
