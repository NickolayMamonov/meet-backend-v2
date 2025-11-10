package dev.whysoezzy.meet.domain.entity

import jakarta.persistence.*
import java.time.Instant

@Entity
@Table(name = "sms_codes")
class SmsCode(
    
    @Id
    @Column(name = "phone_number", nullable = false, length = 20)
    var phoneNumber: String,
    
    @Column(nullable = false, length = 6)
    var code: String,
    
    @Column(name = "expires_at", nullable = false)
    var expiresAt: Instant,
    
    @Column(nullable = false)
    var attempts: Int = 0,
    
    @Column(name = "created_at", nullable = false)
    var createdAt: Instant = Instant.now()
) {
    
    fun isExpired(): Boolean {
        return Instant.now().isAfter(expiresAt)
    }
    
    fun incrementAttempts() {
        attempts++
    }
    
    fun hasExceededAttempts(): Boolean {
        return attempts >= 5
    }
}
