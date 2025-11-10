package dev.whysoezzy.meet.api.dto.user

import dev.whysoezzy.meet.api.dto.common.SocialMediaDto
import dev.whysoezzy.meet.api.dto.common.TagDto
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.Size

// Response DTOs

data class UserProfileDto(
    val id: Long,
    val name: String,
    val surname: String,
    val phoneNumber: String,
    val email: String?,
    val imageUrl: String?,
    val city: String?,
    val description: String?,
    val interests: List<TagDto>,
    val socials: SocialMediaDto?
)

// Request DTOs

data class UpdateProfileRequest(
    @field:Size(min = 2, max = 50, message = "Name must be between 2 and 50 characters")
    val name: String? = null,
    
    @field:Size(min = 2, max = 50, message = "Surname must be between 2 and 50 characters")
    val surname: String? = null,
    
    @field:Email(message = "Invalid email format")
    val email: String? = null,
    
    @field:Size(max = 100, message = "City name cannot exceed 100 characters")
    val city: String? = null,
    
    @field:Size(max = 500, message = "Description cannot exceed 500 characters")
    val description: String? = null
)

data class UpdateInterestsRequest(
    val tagIds: List<Long>
)

data class UpdateSocialsRequest(
    val telegram: String? = null,
    val habr: String? = null
)

data class UploadAvatarResponse(
    val imageUrl: String
)
