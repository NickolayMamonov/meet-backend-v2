package dev.whysoezzy.meet.common.exception

open class BusinessException(
    message: String,
    val code: String = "BUSINESS_ERROR"
) : RuntimeException(message)

class ResourceNotFoundException(
    message: String,
    code: String = "NOT_FOUND"
) : BusinessException(message, code)

class ValidationException(
    message: String,
    val details: Map<String, String>? = null
) : BusinessException(message, "VALIDATION_ERROR")

class ConflictException(
    message: String,
    code: String = "CONFLICT"
) : BusinessException(message, code)

class ForbiddenException(
    message: String = "Access denied"
) : BusinessException(message, "FORBIDDEN")

class UnauthorizedException(
    message: String = "Unauthorized"
) : BusinessException(message, "UNAUTHORIZED")

class BadRequestException(
    message: String,
    code: String = "BAD_REQUEST"
) : BusinessException(message, code)

class TooManyRequestsException(
    message: String = "Too many requests. Please try again later."
) : BusinessException(message, "TOO_MANY_REQUESTS")

class ServiceUnavailableException(
    message: String = "Service temporarily unavailable"
) : BusinessException(message, "SERVICE_UNAVAILABLE")
