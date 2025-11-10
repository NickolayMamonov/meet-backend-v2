package dev.whysoezzy.meet.api.controller

import dev.whysoezzy.meet.api.dto.auth.*
import dev.whysoezzy.meet.api.dto.common.ApiResponse
import dev.whysoezzy.meet.service.AuthService
import io.swagger.v3.oas.annotations.Operation
import jakarta.validation.Valid
import mu.KotlinLogging
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*

private val logger = KotlinLogging.logger {}

@RestController
@RequestMapping("/api/v1/auth")
@io.swagger.v3.oas.annotations.tags.Tag(name = "Authentication", description = "Authentication and authorization endpoints")
class AuthController(
    private val authService: AuthService
) {
    
    @PostMapping("/send-code")
    @ResponseStatus(HttpStatus.OK)
    @Operation(
        summary = "Send verification code",
        description = "Sends a 6-digit verification code to the provided phone number via SMS"
    )
    fun sendCode(
        @Valid @RequestBody request: SendCodeRequest
    ): ApiResponse<Unit> {
        logger.info { "POST /api/v1/auth/send-code - phoneNumber: ${request.phoneNumber}" }
        
        authService.sendVerificationCode(request.phoneNumber)
        
        return ApiResponse.success(
            data = Unit,
            message = "Verification code sent successfully"
        )
    }
    
    @PostMapping("/verify-code")
    @ResponseStatus(HttpStatus.OK)
    @Operation(
        summary = "Verify code",
        description = "Verifies the SMS code and returns a temporary token for registration or login"
    )
    fun verifyCode(
        @Valid @RequestBody request: VerifyCodeRequest
    ): ApiResponse<VerifyCodeResponse> {
        logger.info { "POST /api/v1/auth/verify-code - phoneNumber: ${request.phoneNumber}" }
        
        val response = authService.verifyCode(request.phoneNumber, request.code)
        
        return ApiResponse.success(
            data = response,
            message = "Code verified successfully"
        )
    }
    
    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(
        summary = "Complete registration",
        description = "Completes user registration with name, surname and optional interests"
    )
    fun register(
        @RequestHeader("Authorization") authHeader: String,
        @Valid @RequestBody request: CompleteRegistrationRequest
    ): ApiResponse<LoginResponse> {
        logger.info { "POST /api/v1/auth/register - name: ${request.name}, surname: ${request.surname}" }
        
        val temporaryToken = extractToken(authHeader)
        val response = authService.completeRegistration(
            temporaryToken = temporaryToken,
            name = request.name,
            surname = request.surname,
            interests = request.interests
        )
        
        return ApiResponse.success(
            data = response,
            message = "Registration completed successfully"
        )
    }
    
    @PostMapping("/login")
    @ResponseStatus(HttpStatus.OK)
    @Operation(
        summary = "Login",
        description = "Login for existing users after code verification"
    )
    fun login(
        @RequestHeader("Authorization") authHeader: String
    ): ApiResponse<LoginResponse> {
        logger.info { "POST /api/v1/auth/login" }
        
        val temporaryToken = extractToken(authHeader)
        val response = authService.login(temporaryToken)
        
        return ApiResponse.success(
            data = response,
            message = "Login successful"
        )
    }
    
    @PostMapping("/refresh")
    @ResponseStatus(HttpStatus.OK)
    @Operation(
        summary = "Refresh token",
        description = "Refreshes access token using refresh token"
    )
    fun refreshToken(
        @Valid @RequestBody request: RefreshTokenRequest
    ): ApiResponse<RefreshTokenResponse> {
        logger.info { "POST /api/v1/auth/refresh" }
        
        val response = authService.refreshToken(request.refreshToken)
        
        return ApiResponse.success(
            data = response,
            message = "Token refreshed successfully"
        )
    }
    
    private fun extractToken(authHeader: String): String {
        if (!authHeader.startsWith("Bearer ")) {
            throw IllegalArgumentException("Invalid Authorization header format")
        }
        return authHeader.substring(7)
    }
}
