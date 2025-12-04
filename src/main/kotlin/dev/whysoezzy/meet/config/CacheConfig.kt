package dev.whysoezzy.meet.config

import com.fasterxml.jackson.annotation.JsonTypeInfo
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.KotlinModule
import mu.KotlinLogging
import org.springframework.cache.CacheManager
import org.springframework.cache.annotation.EnableCaching
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.redis.cache.RedisCacheConfiguration
import org.springframework.data.redis.cache.RedisCacheManager
import org.springframework.data.redis.connection.RedisConnectionFactory
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer
import org.springframework.data.redis.serializer.RedisSerializationContext
import org.springframework.data.redis.serializer.StringRedisSerializer
import java.time.Duration

private val logger = KotlinLogging.logger {}

@Configuration
@EnableCaching
class CacheConfig {
    
    init {
        logger.info { "========================================" }
        logger.info { "!!! CACHE CONFIG INITIALIZING !!!" }
        logger.info { "========================================" }
    }
    
    /**
     * Redis Cache Manager with custom configurations
     */
    @Bean
    fun cacheManager(
        redisConnectionFactory: RedisConnectionFactory
    ): CacheManager {
        logger.info { "========================================" }
        logger.info { "!!! CREATING CACHE MANAGER !!!" }
        logger.info { "Redis connection factory: $redisConnectionFactory" }
        logger.info { "========================================" }
        
        // Define cache configurations with different TTLs
        val cacheConfigurations = mapOf(
            // Tags cache - long TTL (rarely changes)
            "tags" to cacheConfiguration(Duration.ofHours(24)),
            
            // Main screen cache - short TTL (frequently updated)
            "main-screen" to cacheConfiguration(Duration.ofMinutes(5)),
            
            // Meetings cache - medium TTL
            "meetings" to cacheConfiguration(Duration.ofMinutes(10)),
            "meeting-detail" to cacheConfiguration(Duration.ofMinutes(10)),
            
            // Communities cache - medium TTL
            "communities" to cacheConfiguration(Duration.ofMinutes(10)),
            "community-detail" to cacheConfiguration(Duration.ofMinutes(10)),
            
            // User cache - long TTL
            "users" to cacheConfiguration(Duration.ofHours(1))
        )
        
        logger.info { "Configured caches: ${cacheConfigurations.keys}" }
        
        val cacheManager = RedisCacheManager.builder(redisConnectionFactory)
            .cacheDefaults(defaultCacheConfiguration())
            .withInitialCacheConfigurations(cacheConfigurations)
            .build()
            
        logger.info { "!!! CACHE MANAGER CREATED SUCCESSFULLY !!!" }
        
        return cacheManager
    }
    
    /**
     * Default cache configuration (5 minutes TTL)
     */
    private fun defaultCacheConfiguration(): RedisCacheConfiguration {
        return RedisCacheConfiguration.defaultCacheConfig()
            .entryTtl(Duration.ofMinutes(5))
            .serializeKeysWith(
                RedisSerializationContext.SerializationPair.fromSerializer(
                    StringRedisSerializer()
                )
            )
            .serializeValuesWith(
                RedisSerializationContext.SerializationPair.fromSerializer(
                    GenericJackson2JsonRedisSerializer(objectMapper())
                )
            )
            .disableCachingNullValues()
    }
    
    /**
     * Custom cache configuration with specific TTL
     */
    private fun cacheConfiguration(ttl: Duration): RedisCacheConfiguration {
        return defaultCacheConfiguration()
            .entryTtl(ttl)
    }
    
    /**
     * ObjectMapper for Redis serialization with proper type handling
     */
    private fun objectMapper(): ObjectMapper {
        return ObjectMapper().apply {
            registerModule(KotlinModule.Builder().build())
            registerModule(JavaTimeModule())
            disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
            
            // Enable default typing with JAVA_LANG_OBJECT as property
//            activateDefaultTyping(
//                LaissezFaireSubTypeValidator.instance,
//                ObjectMapper.DefaultTyping.NON_FINAL,
//                JsonTypeInfo.As.PROPERTY
//            )
        }
    }
}
