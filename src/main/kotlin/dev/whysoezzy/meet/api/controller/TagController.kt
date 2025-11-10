package dev.whysoezzy.meet.api.controller

import dev.whysoezzy.meet.api.dto.common.ApiResponse
import dev.whysoezzy.meet.api.dto.common.TagDto
import dev.whysoezzy.meet.service.TagService
import io.swagger.v3.oas.annotations.Operation
import mu.KotlinLogging
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

private val logger = KotlinLogging.logger {}

@RestController
@RequestMapping("/api/v1/tags")
@io.swagger.v3.oas.annotations.tags.Tag(name = "Tags", description = "Tag management endpoints")
class TagController(
    private val tagService: TagService
) {
    
    @GetMapping
    @Operation(
        summary = "Get all tags",
        description = "Returns all active tags available in the system"
    )
    fun getAllTags(): ApiResponse<List<TagDto>> {
        logger.info { "GET /api/v1/tags" }
        
        val tags = tagService.getAllActiveTags()
        
        return ApiResponse.success(
            data = tags,
            message = "Tags retrieved successfully"
        )
    }
}
