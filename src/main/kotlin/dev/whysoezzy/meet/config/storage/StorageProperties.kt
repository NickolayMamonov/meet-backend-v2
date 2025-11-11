package dev.whysoezzy.meet.config.storage

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "storage")
data class StorageProperties(
    val type: StorageType = StorageType.LOCAL,
    val local: LocalStorageProperties = LocalStorageProperties(),
    val s3: S3StorageProperties = S3StorageProperties()
)

data class LocalStorageProperties(
    val uploadDir: String = "uploads",
    val baseUrl: String = "http://localhost:8080"
)

data class S3StorageProperties(
    val bucket: String = "",
    val region: String = "us-east-1",
    val accessKey: String = "",
    val secretKey: String = "",
    val endpoint: String? = null, // for MinIO compatibility
    val publicUrl: String? = null // CDN URL
)

enum class StorageType {
    LOCAL,
    S3
}
