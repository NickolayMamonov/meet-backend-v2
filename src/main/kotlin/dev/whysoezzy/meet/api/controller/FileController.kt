package dev.whysoezzy.meet.api.controller

import dev.whysoezzy.meet.api.dto.common.ApiResponse
import dev.whysoezzy.meet.api.dto.user.UploadAvatarResponse
import dev.whysoezzy.meet.service.storage.FileStorageService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import mu.KotlinLogging
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile

private val logger = KotlinLogging.logger {}

@RestController
@RequestMapping("/api/v1/files")
@io.swagger.v3.oas.annotations.tags.Tag(name = "Files", description = "File upload and management endpoints")
@SecurityRequirement(name = "Bearer Authentication")
class FileController(
    private val fileStorageService: FileStorageService
) {
    
    @PostMapping("/upload", consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    @Operation(
        summary = "Upload file",
        description = "Upload an image file. Supports JPEG, PNG, GIF, WebP. Max size: 10MB. Requires authentication."
    )
    fun uploadFile(
        @RequestParam("file") file: MultipartFile,
        @RequestParam(defaultValue = "general") folder: String
    ): ApiResponse<UploadAvatarResponse> {
        logger.info { "POST /api/v1/files/upload - filename: ${file.originalFilename}, folder: $folder" }
        
        val fileUrl = fileStorageService.store(file, folder)
        
        return ApiResponse.success(
            data = UploadAvatarResponse(fileUrl),
            message = "File uploaded successfully"
        )
    }
}
