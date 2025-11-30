package dev.whysoezzy.meet.service.storage

import dev.whysoezzy.meet.common.exception.BadRequestException
import dev.whysoezzy.meet.config.storage.StorageProperties
import mu.KotlinLogging
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import java.io.File
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.StandardCopyOption
import java.util.*

private val logger = KotlinLogging.logger {}

@Service
@ConditionalOnProperty(name = ["storage.type"], havingValue = "LOCAL", matchIfMissing = true)
class LocalFileStorageService(
    private val storageProperties: StorageProperties
) : FileStorageService {
    
    private val uploadPath: Path
    
    init {
        uploadPath = Paths.get(storageProperties.local.uploadDir)
        try {
            Files.createDirectories(uploadPath)
            logger.info { "Local storage initialized at: ${uploadPath.toAbsolutePath()}" }
        } catch (e: Exception) {
            logger.error(e) { "Failed to create upload directory" }
            throw RuntimeException("Could not initialize storage", e)
        }
    }
    
    override fun store(file: MultipartFile, folder: String): String {
        validate(file)
        
        val filename = generateFilename(file)
        val folderPath = uploadPath.resolve(folder)
        Files.createDirectories(folderPath)
        
        val destinationFile = folderPath.resolve(filename)
        
        try {
            file.inputStream.use { inputStream ->
                Files.copy(inputStream, destinationFile, StandardCopyOption.REPLACE_EXISTING)
            }
            
            val url = "${storageProperties.local.baseUrl}/uploads/$folder/$filename"
            logger.info { "File stored locally: $url" }
            
            return url
        } catch (e: Exception) {
            logger.error(e) { "Failed to store file: ${file.originalFilename}" }
            throw RuntimeException("Failed to store file", e)
        }
    }
    
    override fun delete(fileUrl: String) {
        try {
            // Extract path from URL
            val path = fileUrl.substringAfter("/uploads/")
            val file = uploadPath.resolve(path)
            
            if (Files.exists(file)) {
                Files.delete(file)
                logger.info { "File deleted: $path" }
            }
        } catch (e: Exception) {
            logger.error(e) { "Failed to delete file: $fileUrl" }
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
