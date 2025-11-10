package dev.whysoezzy.meet.domain.repository

import dev.whysoezzy.meet.domain.entity.SmsCode
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.time.Instant

@Repository
interface SmsCodeRepository : JpaRepository<SmsCode, String> {
    
    fun findByPhoneNumber(phoneNumber: String): SmsCode?
    
    @Modifying
    @Query("DELETE FROM SmsCode s WHERE s.expiresAt < :now")
    fun deleteExpiredCodes(@Param("now") now: Instant): Int
}
