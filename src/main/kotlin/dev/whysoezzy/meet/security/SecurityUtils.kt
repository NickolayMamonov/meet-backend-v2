package dev.whysoezzy.meet.security

import dev.whysoezzy.meet.common.exception.UnauthorizedException
import org.springframework.security.core.context.SecurityContextHolder

object SecurityUtils {
    
    /**
     * Get current authenticated user ID
     * @throws UnauthorizedException if user is not authenticated
     */
    fun getCurrentUserId(): Long {
        val authentication = SecurityContextHolder.getContext().authentication
            ?: throw UnauthorizedException("User is not authenticated")
        
        if (!authentication.isAuthenticated || authentication.principal == "anonymousUser") {
            throw UnauthorizedException("User is not authenticated")
        }
        
        return when (val principal = authentication.principal) {
            is String -> principal.toLongOrNull()
                ?: throw UnauthorizedException("Invalid user ID format: $principal")
            is Long -> principal
            is Number -> principal.toLong()
            else -> throw UnauthorizedException("Unknown principal type: ${principal?.javaClass?.simpleName}")
        }
    }
    
    /**
     * Get current authenticated user ID or null if not authenticated
     */
    fun getCurrentUserIdOrNull(): Long? {
        return try {
            val authentication = SecurityContextHolder.getContext().authentication
            if (authentication?.isAuthenticated == true && authentication.principal != "anonymousUser") {
                when (val principal = authentication.principal) {
                    is String -> principal.toLongOrNull()
                    is Long -> principal
                    is Number -> principal.toLong()
                    else -> null
                }
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }
    
    /**
     * Check if user is authenticated
     */
    fun isAuthenticated(): Boolean {
        val authentication = SecurityContextHolder.getContext().authentication
        return authentication?.isAuthenticated == true && authentication.principal != "anonymousUser"
    }
}
