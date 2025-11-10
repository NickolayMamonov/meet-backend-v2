package dev.whysoezzy.meet.api.dto.auth

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Size

data class SendCodeRequest(
    @field:NotBlank(message = "Phone number is required")
    @field:Pattern(regexp = "^\\+7[0-9]{10}\$", message = "Phone number must be in format +7XXXXXXXXXX")
    val phoneNumber: String
)

data class VerifyCodeRequest(
    @field:NotBlank(message = "Phone number is required")
    @field:Pattern(regexp = "^\\+7[0-9]{10}\$", message = "Phone number must be in format +7XXXXXXXXXX")
    val phoneNumber: String,
    
    @field:NotBlank(message = "Code is required")
    @field:Pattern(regexp = "^[0-9]{6}\$", message = "Code must be 6 digits")
    val code: String
)

data class VerifyCodeResponse(
    val temporaryToken: String,
    val isNewUser: Boolean
)

data class CompleteRegistrationRequest(
    @field:NotBlank(message = "Name is required")
    @field:Size(min = 2, max = 100, message = "Name must be between 2 and 100 characters")
    val name: String,
    
    @field:NotBlank(message = "Surname is required")
    @field:Size(min = 2, max = 100, message = "Surname must be between 2 and 100 characters")
    val surname: String,
    
    val interests: List<Long>? = null
)

data class LoginResponse(
    val accessToken: String,
    val refreshToken: String,
    val user: UserAuthDto
)

data class UserAuthDto(
    val id: Long,
    val name: String,
    val surname: String,
    val phoneNumber: String,
    val imageUrl: String?,
    val city: String,
    val description: String
)

data class RefreshTokenRequest(
    @field:NotBlank(message = "Refresh token is required")
    val refreshToken: String
)

data class RefreshTokenResponse(
    val accessToken: String,
    val refreshToken: String
)
