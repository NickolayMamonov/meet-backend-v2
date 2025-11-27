package dev.whysoezzy.meet.config.notification

import com.google.auth.oauth2.GoogleCredentials
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import mu.KotlinLogging
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.io.ByteArrayInputStream
import java.io.FileInputStream

private val logger = KotlinLogging.logger {}

@ConfigurationProperties(prefix = "firebase")
data class FirebaseProperties(
    val enabled: Boolean = false,
    val credentialsPath: String? = null,
    val credentialsJson: String? = null,
    val projectId: String? = null
)

@Configuration
@EnableConfigurationProperties(FirebaseProperties::class)
class FirebaseConfig(
    private val firebaseProperties: FirebaseProperties
) {
    
    @Bean
    fun firebaseApp(): FirebaseApp? {
        if (!firebaseProperties.enabled) {
            logger.info { "Firebase is disabled. Push notifications will not be sent." }
            return null
        }
        
        return try {
            val credentials = when {
                !firebaseProperties.credentialsJson.isNullOrBlank() -> {
                    logger.info { "Initializing Firebase with JSON credentials" }
                    GoogleCredentials.fromStream(
                        ByteArrayInputStream(firebaseProperties.credentialsJson.toByteArray())
                    )
                }
                !firebaseProperties.credentialsPath.isNullOrBlank() -> {
                    logger.info { "Initializing Firebase with credentials file: ${firebaseProperties.credentialsPath}" }
                    GoogleCredentials.fromStream(FileInputStream(firebaseProperties.credentialsPath))
                }
                else -> {
                    logger.warn { "No Firebase credentials provided. Using application default credentials." }
                    GoogleCredentials.getApplicationDefault()
                }
            }
            
            val options = FirebaseOptions.builder()
                .setCredentials(credentials)
                .apply {
                    firebaseProperties.projectId?.let { setProjectId(it) }
                }
                .build()
            
            FirebaseApp.initializeApp(options).also {
                logger.info { "Firebase initialized successfully" }
            }
        } catch (e: Exception) {
            logger.error(e) { "Failed to initialize Firebase" }
            null
        }
    }
}
