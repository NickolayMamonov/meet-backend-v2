package dev.whysoezzy.meet.service

import dev.whysoezzy.meet.api.dto.common.SocialMediaDto
import dev.whysoezzy.meet.api.dto.common.TagDto
import dev.whysoezzy.meet.api.dto.user.*
import dev.whysoezzy.meet.common.exception.ResourceNotFoundException
import dev.whysoezzy.meet.domain.entity.User
import dev.whysoezzy.meet.domain.repository.TagRepository
import dev.whysoezzy.meet.domain.repository.UserRepository
import dev.whysoezzy.meet.security.SecurityUtils
import dev.whysoezzy.meet.service.storage.FileStorageService
import mu.KotlinLogging
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.multipart.MultipartFile

private val logger = KotlinLogging.logger {}

@Service
class UserService(
    private val userRepository: UserRepository,
    private val tagRepository: TagRepository,
    private val fileStorageService: FileStorageService
) {
    
    @Transactional(readOnly = true)
    fun getCurrentUserProfile(): UserProfileDto {
        val currentUserId = SecurityUtils.getCurrentUserId()
        logger.info { "Fetching profile for user: $currentUserId" }
        
        val user = userRepository.findById(currentUserId).orElseThrow {
            ResourceNotFoundException("User not found")
        }
        
        return user.toProfileDto()
    }
    
    @Transactional
    fun updateProfile(request: UpdateProfileRequest): UserProfileDto {
        val currentUserId = SecurityUtils.getCurrentUserId()
        logger.info { "Updating profile for user: $currentUserId" }
        
        val user = userRepository.findById(currentUserId).orElseThrow {
            ResourceNotFoundException("User not found")
        }
        
        // Update fields if provided
        request.name?.let { user.name = it }
        request.surname?.let { user.surname = it }
        request.email?.let { user.email = it }
        request.city?.let { user.city = it }
        request.description?.let { user.description = it }
        
        val updatedUser = userRepository.save(user)
        
        logger.info { "Profile updated successfully for user: $currentUserId" }
        
        return updatedUser.toProfileDto()
    }
    
    @Transactional
    fun updateInterests(request: UpdateInterestsRequest): UserProfileDto {
        val currentUserId = SecurityUtils.getCurrentUserId()
        logger.info { "Updating interests for user: $currentUserId" }
        
        val user = userRepository.findById(currentUserId).orElseThrow {
            ResourceNotFoundException("User not found")
        }
        
        // Clear existing interests
        user.interests.clear()
        
        // Add new interests
        val tags = tagRepository.findAllById(request.tagIds)
        user.interests.addAll(tags)
        
        val updatedUser = userRepository.save(user)
        
        logger.info { "Interests updated successfully for user: $currentUserId" }
        
        return updatedUser.toProfileDto()
    }
    
    @Transactional
    fun updateSocials(request: UpdateSocialsRequest): UserProfileDto {
        val currentUserId = SecurityUtils.getCurrentUserId()
        logger.info { "Updating socials for user: $currentUserId" }
        
        val user = userRepository.findById(currentUserId).orElseThrow {
            ResourceNotFoundException("User not found")
        }
        
        // Update social media links
        request.telegram?.let { user.telegram = it }
        request.habr?.let { user.habr = it }
        
        val updatedUser = userRepository.save(user)
        
        logger.info { "Socials updated successfully for user: $currentUserId" }
        
        return updatedUser.toProfileDto()
    }
    
    @Transactional
    fun uploadAvatar(file: MultipartFile): UploadAvatarResponse {
        val currentUserId = SecurityUtils.getCurrentUserId()
        logger.info { "Uploading avatar for user: $currentUserId" }
        
        val user = userRepository.findById(currentUserId).orElseThrow {
            ResourceNotFoundException("User not found")
        }
        
        // Delete old avatar if exists
        user.imageUrl?.let { oldImageUrl ->
            try {
                fileStorageService.delete(oldImageUrl)
            } catch (e: Exception) {
                logger.warn { "Failed to delete old avatar: ${e.message}" }
            }
        }
        
        // Upload new avatar
        val imageUrl = fileStorageService.store(file, "avatars")
        
        user.imageUrl = imageUrl
        userRepository.save(user)
        
        logger.info { "Avatar uploaded successfully for user: $currentUserId" }
        
        return UploadAvatarResponse(imageUrl)
    }
    
    @Transactional
    fun registerFcmToken(fcmToken: String) {
        val currentUserId = SecurityUtils.getCurrentUserId()
        logger.info { "Registering FCM token for user: $currentUserId" }
        
        val user = userRepository.findById(currentUserId).orElseThrow {
            ResourceNotFoundException("User not found")
        }
        
        user.fcmToken = fcmToken
        userRepository.save(user)
        
        logger.info { "FCM token registered successfully for user: $currentUserId" }
    }
    
    @Transactional
    fun unregisterFcmToken() {
        val currentUserId = SecurityUtils.getCurrentUserId()
        logger.info { "Unregistering FCM token for user: $currentUserId" }
        
        val user = userRepository.findById(currentUserId).orElseThrow {
            ResourceNotFoundException("User not found")
        }
        
        user.fcmToken = null
        userRepository.save(user)
        
        logger.info { "FCM token unregistered successfully for user: $currentUserId" }
    }
    
    // Extension function for mapping
    
    private fun User.toProfileDto(): UserProfileDto {
        return UserProfileDto(
            id = id!!,
            name = name,
            surname = surname,
            phoneNumber = phoneNumber,
            email = email,
            imageUrl = imageUrl,
            city = city,
            description = description,
            interests = interests.map { TagDto(it.id!!, it.text, it.state.name) },
            socials = if (telegram != null || habr != null) {
                SocialMediaDto(telegram, habr)
            } else null
        )
    }
}
