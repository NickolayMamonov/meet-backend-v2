package dev.whysoezzy.meet.config

import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.nio.charset.StandardCharsets
import java.util.*
import javax.crypto.SecretKey

@Component
class JwtTokenProvider(
    @Value("\${app.jwt.secret}")
    private val secret: String,
    
    @Value("\${app.jwt.access-token-expiration}")
    private val accessTokenExpiration: Long,
    
    @Value("\${app.jwt.refresh-token-expiration}")
    private val refreshTokenExpiration: Long
) {
    
    private val secretKey: SecretKey by lazy {
        Keys.hmacShaKeyFor(secret.toByteArray(StandardCharsets.UTF_8))
    }
    
    fun generateAccessToken(userId: Long): String {
        return generateToken(userId, "access", accessTokenExpiration)
    }
    
    fun generateRefreshToken(userId: Long): String {
        return generateToken(userId, "refresh", refreshTokenExpiration)
    }
    
    fun generateTemporaryToken(phoneNumber: String): String {
        val now = Date()
        val expiryDate = Date(now.time + 600000) // 10 minutes
        
        return Jwts.builder()
            .subject(phoneNumber)
            .claim("type", "temporary")
            .issuedAt(now)
            .expiration(expiryDate)
            .signWith(secretKey)
            .compact()
    }
    
    private fun generateToken(userId: Long, type: String, expiration: Long): String {
        val now = Date()
        val expiryDate = Date(now.time + expiration)
        
        return Jwts.builder()
            .subject(userId.toString())
            .claim("type", type)
            .issuedAt(now)
            .expiration(expiryDate)
            .signWith(secretKey)
            .compact()
    }
    
    fun getUserIdFromToken(token: String): Long? {
        return try {
            val claims = getClaims(token)
            claims.subject.toLongOrNull()
        } catch (e: Exception) {
            null
        }
    }
    
    fun getPhoneNumberFromTemporaryToken(token: String): String? {
        return try {
            val claims = getClaims(token)
            if (claims["type"] == "temporary") {
                claims.subject
            } else null
        } catch (e: Exception) {
            null
        }
    }
    
    fun validateToken(token: String): Boolean {
        return try {
            getClaims(token)
            true
        } catch (e: Exception) {
            false
        }
    }
    
    fun isAccessToken(token: String): Boolean {
        return try {
            val claims = getClaims(token)
            claims["type"] == "access"
        } catch (e: Exception) {
            false
        }
    }
    
    fun isRefreshToken(token: String): Boolean {
        return try {
            val claims = getClaims(token)
            claims["type"] == "refresh"
        } catch (e: Exception) {
            false
        }
    }
    
    private fun getClaims(token: String): Claims {
        return Jwts.parser()
            .verifyWith(secretKey)
            .build()
            .parseSignedClaims(token)
            .payload
    }
    
    fun getExpirationDate(token: String): Date? {
        return try {
            getClaims(token).expiration
        } catch (e: Exception) {
            null
        }
    }
}
