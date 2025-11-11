package dev.whysoezzy.meet.service.storage

import org.springframework.web.multipart.MultipartFile

interface FileStorageService {
    
    /**
     * Store file and return its URL
     */
    fun store(file: MultipartFile, folder: String): String
    
    /**
     * Delete file by URL
     */
    fun delete(fileUrl: String)
    
    /**
     * Get file content type
     */
    fun getContentType(file: MultipartFile): String
    
    /**
     * Validate file (size, type)
     */
    fun validate(file: MultipartFile)
}

data class FileMetadata(
    val originalFilename: String,
    val contentType: String,
    val size: Long,
    val url: String
)
