package dev.whysoezzy.meet.api.dto.common

import com.fasterxml.jackson.annotation.JsonInclude

@JsonInclude(JsonInclude.Include.NON_NULL)
data class ApiResponse<T>(
    val success: Boolean,
    val data: T? = null,
    val error: ErrorResponse? = null,
    val message: String? = null
) {
    companion object {
        fun <T> success(data: T, message: String? = null): ApiResponse<T> {
            return ApiResponse(success = true, data = data, message = message)
        }
        
        fun <T> error(code: String, message: String, details: Map<String, Any>? = null): ApiResponse<T> {
            return ApiResponse(
                success = false,
                error = ErrorResponse(code, message, details)
            )
        }
    }
}

data class ErrorResponse(
    val code: String,
    val message: String,
    val details: Map<String, Any>? = null
)

data class PaginationResponse<T>(
    val items: List<T>,
    val page: Int,
    val limit: Int,
    val total: Long,
    val hasMore: Boolean
)

data class TagDto(
    val id: Long,
    val text: String,
    val state: String
)

data class AddressDto(
    val address: String,
    val latitude: Double,
    val longitude: Double
)

data class SocialMediaDto(
    val telegram: String? = null,
    val habr: String? = null
)
