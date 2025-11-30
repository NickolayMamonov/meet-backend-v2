package dev.whysoezzy.meet.api.controller

import dev.whysoezzy.meet.api.dto.common.ApiResponse
import dev.whysoezzy.meet.security.SecurityUtils
import dev.whysoezzy.meet.service.notification.NotificationType
import dev.whysoezzy.meet.service.notification.PushNotificationData
import dev.whysoezzy.meet.service.notification.PushNotificationService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import mu.KotlinLogging
import org.springframework.web.bind.annotation.*

private val logger = KotlinLogging.logger {}

@RestController
@RequestMapping("/api/v1/notifications")
@io.swagger.v3.oas.annotations.tags.Tag(name = "Notifications", description = "Push notification testing endpoints")
@SecurityRequirement(name = "Bearer Authentication")
class NotificationController(
    private val pushNotificationService: PushNotificationService
) {
    
    @PostMapping("/test")
    @Operation(
        summary = "Send test notification",
        description = "Sends a test push notification to the current user. FOR TESTING ONLY. Requires authentication and registered FCM token."
    )
    fun sendTestNotification(): ApiResponse<String> {
        val currentUserId = SecurityUtils.getCurrentUserId()
        logger.info { "POST /api/v1/notifications/test - user: $currentUserId" }
        
        pushNotificationService.sendToUser(
            userId = currentUserId,
            title = "🎉 Test Notification",
            body = "This is a test push notification from Meet Backend!",
            data = PushNotificationData(
                type = NotificationType.COMMENT,
                entityId = 1,
                entityType = "TEST"
            ).toMap()
        )
        
        return ApiResponse.success(
            data = "Test notification sent to user $currentUserId",
            message = "Notification queued successfully"
        )
    }
    
    @PostMapping("/test-meeting-reminder")
    @Operation(
        summary = "Send test meeting reminder",
        description = "Sends a test meeting reminder notification. FOR TESTING ONLY."
    )
    fun sendTestMeetingReminder(): ApiResponse<String> {
        val currentUserId = SecurityUtils.getCurrentUserId()
        logger.info { "POST /api/v1/notifications/test-meeting-reminder - user: $currentUserId" }
        
        pushNotificationService.sendToUser(
            userId = currentUserId,
            title = "📅 Meeting Reminder",
            body = "Kotlin Meetup Moscow starts in 1 hour!",
            data = PushNotificationData(
                type = NotificationType.MEETING_REMINDER,
                entityId = 123,
                entityType = "MEETING"
            ).toMap()
        )
        
        return ApiResponse.success(
            data = "Meeting reminder sent to user $currentUserId",
            message = "Notification queued successfully"
        )
    }
}
