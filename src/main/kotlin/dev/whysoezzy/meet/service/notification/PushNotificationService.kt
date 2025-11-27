package dev.whysoezzy.meet.service.notification

import com.google.firebase.FirebaseApp
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.Message
import com.google.firebase.messaging.Notification
import dev.whysoezzy.meet.domain.repository.UserRepository
import mu.KotlinLogging
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Service

private val logger = KotlinLogging.logger {}

@Service
class PushNotificationService(
    private val firebaseApp: FirebaseApp?,
    private val userRepository: UserRepository
) {
    
    /**
     * Send push notification to a single user
     */
    @Async
    fun sendToUser(
        userId: Long,
        title: String,
        body: String,
        data: Map<String, String> = emptyMap()
    ) {
        if (firebaseApp == null) {
            logger.debug { "Firebase not initialized. Skipping push notification to user $userId" }
            return
        }
        
        try {
            val user = userRepository.findById(userId).orElse(null)
            if (user == null) {
                logger.debug { "User $userId not found. Skipping notification." }
                return
            }
            
            val fcmToken = user.fcmToken
            if (fcmToken.isNullOrBlank()) {
                logger.debug { "User $userId has no FCM token. Skipping notification." }
                return
            }
            
            sendNotification(fcmToken, title, body, data)
            logger.info { "Push notification sent to user $userId: $title" }
            
        } catch (e: Exception) {
            logger.error(e) { "Failed to send push notification to user $userId" }
        }
    }
    
    /**
     * Send push notification to multiple users
     */
    @Async
    fun sendToUsers(
        userIds: List<Long>,
        title: String,
        body: String,
        data: Map<String, String> = emptyMap()
    ) {
        if (firebaseApp == null) {
            logger.debug { "Firebase not initialized. Skipping push notifications to ${userIds.size} users" }
            return
        }
        
        try {
            val users = userRepository.findAllById(userIds)
            val tokens = users
                .mapNotNull { it.fcmToken }
                .filter { it.isNotBlank() }
            
            if (tokens.isEmpty()) {
                logger.debug { "No FCM tokens found for ${userIds.size} users. Skipping notifications." }
                return
            }
            
            tokens.forEach { token ->
                try {
                    sendNotification(token, title, body, data)
                } catch (e: Exception) {
                    logger.warn(e) { "Failed to send notification to token: ${token.take(20)}..." }
                }
            }
            
            logger.info { "Push notifications sent to ${tokens.size} users: $title" }
            
        } catch (e: Exception) {
            logger.error(e) { "Failed to send push notifications to users" }
        }
    }
    
    /**
     * Send notification to a specific FCM token
     */
    private fun sendNotification(
        token: String,
        title: String,
        body: String,
        data: Map<String, String>
    ) {
        val notification = Notification.builder()
            .setTitle(title)
            .setBody(body)
            .build()
        
        val messageBuilder = Message.builder()
            .setToken(token)
            .setNotification(notification)
        
        // Add custom data
        if (data.isNotEmpty()) {
            messageBuilder.putAllData(data)
        }
        
        val message = messageBuilder.build()
        
        FirebaseMessaging.getInstance(firebaseApp).send(message)
    }
}

/**
 * Notification types for different events
 */
enum class NotificationType {
    NEW_MEETING,
    MEETING_REMINDER,
    MEETING_CANCELLED,
    NEW_PARTICIPANT,
    COMMUNITY_UPDATE,
    COMMENT,
    MENTION
}

/**
 * Notification data class
 */
data class PushNotificationData(
    val type: NotificationType,
    val entityId: Long,
    val entityType: String
) {
    fun toMap(): Map<String, String> {
        return mapOf(
            "type" to type.name,
            "entityId" to entityId.toString(),
            "entityType" to entityType
        )
    }
}
