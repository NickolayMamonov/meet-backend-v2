package dev.whysoezzy.meet.service.storage

import dev.whysoezzy.meet.common.exception.BadRequestException
import dev.whysoezzy.meet.config.storage.StorageProperties
import mu.KotlinLogging
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import software.amazon.awssdk.core.sync.RequestBody
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest
import software.amazon.awssdk.services.s3.model.PutObjectRequest
import java.util.*

private val logger = KotlinLogging.logger {}

@Service
@ConditionalOnProperty(name = ["storage.type"], havingValue = "S3")
class S3FileStorageService(
    private val s3Client: S3Client,
    private val storageProperties: StorageProperties
) : FileStorageService {
    
    private val bucket = storageProperties.s3.bucket
    private val publicUrl = storageProperties.s3.publicUrl
    
    init {
        logger.info { "S3 storage initialized with bucket: $bucket" }
    }
    
    override fun store(file: MultipartFile, folder: String): String {
        validate(file)
        
        val key = "$folder/${generateFilename(file)}"
        
        try {
            val putObjectRequest = PutObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .contentType(file.contentType)
                .contentLength(file.size)
                .build()
            
            s3Client.putObject(
                putObjectRequest,
                RequestBody.fromInputStream(file.inputStream, file.size)
            )
            
            val url = publicUrl?.let { "$it/$key" }
                ?: "https://$bucket.s3.${storageProperties.s3.region}.amazonaws.com/$key"
            
            logger.info { "File uploaded to S3: $url" }
            
            return url
        } catch (e: Exception) {
            logger.error(e) { "Failed to upload file to S3: ${file.originalFilename}" }
            throw RuntimeException("Failed to upload file", e)
        }
    }
    
    override fun delete(fileUrl: String) {
        try {
            // Extract key from URL
            val key = if (publicUrl != null) {
                fileUrl.substringAfter("$publicUrl/")
            } else {
                fileUrl.substringAfter(".amazonaws.com/")
            }
            
            val deleteObjectRequest = DeleteObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .build()
            
            s3Client.deleteObject(deleteObjectRequest)
            
            logger.info { "File deleted from S3: $key" }
        } catch (e: Exception) {
            logger.error(e) { "Failed to delete file from S3: $fileUrl" }
        }
    }
    
    override fun getContentType(file: MultipartFile): String {
        return file.contentType ?: "application/octet-stream"
    }
    
    override fun validate(file: MultipartFile) {
        // Check if file is empty
        if (file.isEmpty) {
            throw BadRequestException("File is empty")
        }
        
        // Check file size (max 10MB)
        val maxSize = 10 * 1024 * 1024 // 10MB
        if (file.size > maxSize) {
            throw BadRequestException("File size exceeds maximum allowed size of 10MB")
        }
        
        // Check file type (only images)
        val allowedTypes = listOf("image/jpeg", "image/jpg", "image/png", "image/gif", "image/webp")
        val contentType = file.contentType ?: ""
        
        if (contentType !in allowedTypes) {
            throw BadRequestException("Invalid file type. Only images are allowed (JPEG, PNG, GIF, WebP)")
        }
    }
    
    private fun generateFilename(file: MultipartFile): String {
        val extension = file.originalFilename?.substringAfterLast('.', "") ?: "jpg"
        val timestamp = System.currentTimeMillis()
        val randomString = UUID.randomUUID().toString().substring(0, 8)
        return "${timestamp}_${randomString}.$extension"
    }
}
