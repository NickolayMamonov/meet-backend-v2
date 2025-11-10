package dev.whysoezzy.meet.config

import dev.whysoezzy.meet.domain.repository.SmsCodeRepository
import mu.KotlinLogging
import org.springframework.context.annotation.Configuration
import org.springframework.scheduling.annotation.EnableScheduling
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.transaction.annotation.Transactional
import java.time.Instant

private val logger = KotlinLogging.logger {}

@Configuration
@EnableScheduling
class SchedulingConfig(
    private val smsCodeRepository: SmsCodeRepository
) {
    
    @Scheduled(fixedRate = 300000) // Every 5 minutes
    @Transactional
    fun cleanupExpiredSmsCodes() {
        logger.debug { "Running cleanup task for expired SMS codes" }
        
        val deletedCount = smsCodeRepository.deleteExpiredCodes(Instant.now())
        
        if (deletedCount > 0) {
            logger.info { "Deleted $deletedCount expired SMS codes" }
        }
    }
}
