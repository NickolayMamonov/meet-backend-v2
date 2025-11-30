package dev.whysoezzy.meet.api.controller

import dev.whysoezzy.meet.api.dto.common.ApiResponse
import dev.whysoezzy.meet.api.dto.user.*
import dev.whysoezzy.meet.service.UserService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import jakarta.validation.Valid
import mu.KotlinLogging
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile

private val logger = KotlinLogging.logger {}

@RestController
@RequestMapping("/api/v1/users")
@io.swagger.v3.oas.annotations.tags.Tag(name = "Users", description = "User profile management endpoints")
@SecurityRequirement(name = "Bearer Authentication")
class UserController(
    private val userService: UserService
) {
    
    @GetMapping("/me")
    @Operation(
        summary = "Get current user profile",
        description = "Returns the profile of the currently authenticated user. Requires authentication."
    )
    fun getCurrentUserProfile(): ApiResponse<UserProfileDto> {
        logger.info { "GET /api/v1/users/me" }
        
        val profile = userService.getCurrentUserProfile()
        
        return ApiResponse.success(
            data = profile,
            message = "Profile retrieved successfully"
        )
    }
    
    @PutMapping("/me")
    @Operation(
        summary = "Update current user profile",
        description = "Updates profile information (name, surname, email, city, description). Requires authentication."
    )
    fun updateProfile(
        @Valid @RequestBody request: UpdateProfileRequest
    ): ApiResponse<UserProfileDto> {
        logger.info { "PUT /api/v1/users/me" }
        
        val profile = userService.updateProfile(request)
        
        return ApiResponse.success(
            data = profile,
            message = "Profile updated successfully"
        )
    }
    
    @PutMapping("/me/interests")
    @Operation(
        summary = "Update user interests",
        description = "Updates the user's interests (tags). Replaces all existing interests. Requires authentication."
    )
    fun updateInterests(
        @Valid @RequestBody request: UpdateInterestsRequest
    ): ApiResponse<UserProfileDto> {
        logger.info { "PUT /api/v1/users/me/interests" }
        
        val profile = userService.updateInterests(request)
        
        return ApiResponse.success(
            data = profile,
            message = "Interests updated successfully"
        )
    }
    
    @PutMapping("/me/socials")
    @Operation(
        summary = "Update user social media links",
        description = "Updates the user's social media links (Telegram, Habr). Requires authentication."
    )
    fun updateSocials(
        @Valid @RequestBody request: UpdateSocialsRequest
    ): ApiResponse<UserProfileDto> {
        logger.info { "PUT /api/v1/users/me/socials" }
        
        val profile = userService.updateSocials(request)
        
        return ApiResponse.success(
            data = profile,
            message = "Social media links updated successfully"
        )
    }
    
    @PostMapping("/me/avatar", consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    @Operation(
        summary = "Upload user avatar",
        description = "Uploads a new avatar image for the user. Accepts image files. Requires authentication."
    )
    fun uploadAvatar(
        @RequestParam("file") file: MultipartFile
    ): ApiResponse<UploadAvatarResponse> {
        logger.info { "POST /api/v1/users/me/avatar - filename: ${file.originalFilename}" }
        
        val response = userService.uploadAvatar(file)
        
        return ApiResponse.success(
            data = response,
            message = "Avatar uploaded successfully"
        )
    }
    
    @PostMapping("/me/fcm-token")
    @Operation(
        summary = "Register FCM token",
        description = "Registers the user's device FCM token for push notifications. Requires authentication."
    )
    fun registerFcmToken(
        @Valid @RequestBody request: RegisterFcmTokenRequest
    ): ApiResponse<Unit> {
        logger.info { "POST /api/v1/users/me/fcm-token" }
        
        userService.registerFcmToken(request.fcmToken)
        
        return ApiResponse.success(
            data = Unit,
            message = "FCM token registered successfully"
        )
    }
    
    @DeleteMapping("/me/fcm-token")
    @Operation(
        summary = "Unregister FCM token",
        description = "Removes the user's FCM token. Push notifications will stop. Requires authentication."
    )
    fun unregisterFcmToken(): ApiResponse<Unit> {
        logger.info { "DELETE /api/v1/users/me/fcm-token" }
        
        userService.unregisterFcmToken()
        
        return ApiResponse.success(
            data = Unit,
            message = "FCM token unregistered successfully"
        )
    }
}
