package dev.whysoezzy.meet.service

import mu.KotlinLogging
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service

private val logger = KotlinLogging.logger {}

interface SmsService {
    fun sendVerificationCode(phoneNumber: String, code: String)
}

@Service
class ConsoleSmsService : SmsService {
    
    override fun sendVerificationCode(phoneNumber: String, code: String) {
        logger.info { "========================================" }
        logger.info { "SMS CODE FOR $phoneNumber: $code" }
        logger.info { "========================================" }
    }
}

// TODO: Implement real SMS service (Twilio, etc.)
//@Service
//@ConditionalOnProperty(name = ["app.sms.provider"], havingValue = "twilio")
//class TwilioSmsService(
//    @Value("\${app.sms.twilio.account-sid}") private val accountSid: String,
//    @Value("\${app.sms.twilio.auth-token}") private val authToken: String,
//    @Value("\${app.sms.twilio.from-number}") private val fromNumber: String
//) : SmsService {
//    
//    private val client = Twilio.init(accountSid, authToken)
//    
//    override fun sendVerificationCode(phoneNumber: String, code: String) {
//        val message = Message.create(
//            com.twilio.type.PhoneNumber(phoneNumber),
//            com.twilio.type.PhoneNumber(fromNumber),
//            "Your Meet verification code is: $code"
//        ).create()
//        
//        logger.info { "SMS sent to $phoneNumber with SID: ${message.sid}" }
//    }
//}
