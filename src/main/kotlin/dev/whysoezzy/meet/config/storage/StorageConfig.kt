package dev.whysoezzy.meet.config.storage

import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.s3.S3Client
import java.net.URI

@Configuration
@EnableConfigurationProperties(StorageProperties::class)
class StorageConfig(
    private val storageProperties: StorageProperties
) {
    
    @Bean
    fun s3Client(): S3Client {
        val s3Props = storageProperties.s3
        
        val builder = S3Client.builder()
            .region(Region.of(s3Props.region))
        
        // Add credentials if provided
        if (s3Props.accessKey.isNotBlank() && s3Props.secretKey.isNotBlank()) {
            val credentials = AwsBasicCredentials.create(s3Props.accessKey, s3Props.secretKey)
            builder.credentialsProvider(StaticCredentialsProvider.create(credentials))
        }
        
        // Add custom endpoint if provided (for MinIO)
        s3Props.endpoint?.let {
            builder.endpointOverride(URI.create(it))
        }
        
        return builder.build()
    }
}
