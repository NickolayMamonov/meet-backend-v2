package dev.whysoezzy.meet.api.controller

import dev.whysoezzy.meet.api.dto.common.ApiResponse
import dev.whysoezzy.meet.api.dto.common.PaginationResponse
import dev.whysoezzy.meet.api.dto.community.*
import dev.whysoezzy.meet.api.dto.meeting.MeetingDto
import dev.whysoezzy.meet.service.CommunityService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import jakarta.validation.Valid
import mu.KotlinLogging
import org.springframework.data.domain.Page
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*

private val logger = KotlinLogging.logger {}

@RestController
@RequestMapping("/api/v1/communities")
@io.swagger.v3.oas.annotations.tags.Tag(name = "Communities", description = "Community management endpoints")
class CommunityController(
    private val communityService: CommunityService
) {
    
    @GetMapping
    @Operation(
        summary = "Get all communities",
        description = "Returns paginated list of all communities"
    )
    fun getAllCommunities(
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int
    ): ApiResponse<PaginationResponse<CommunityDto>> {
        logger.info { "GET /api/v1/communities - page: $page, size: $size" }
        
        val communities = communityService.getAllCommunities(page, size)
        
        return ApiResponse.success(
            data = communities.toPaginationResponse(),
            message = "Communities retrieved successfully"
        )
    }
    
    @GetMapping("/{id}")
    @Operation(
        summary = "Get community by ID",
        description = "Returns detailed information about a specific community including upcoming meetings"
    )
    fun getCommunityById(@PathVariable id: Long): ApiResponse<CommunityDetailDto> {
        logger.info { "GET /api/v1/communities/$id" }
        
        val community = communityService.getCommunityById(id)
        
        return ApiResponse.success(
            data = community,
            message = "Community retrieved successfully"
        )
    }
    
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(
        summary = "Create community",
        description = "Creates a new community. Requires authentication."
    )
    fun createCommunity(
        @Valid @RequestBody request: CreateCommunityRequest
    ): ApiResponse<CommunityDto> {
        logger.info { "POST /api/v1/communities - title: ${request.title}" }
        
        val community = communityService.createCommunity(request)
        
        return ApiResponse.success(
            data = community,
            message = "Community created successfully"
        )
    }
    
    @PutMapping("/{id}")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(
        summary = "Update community",
        description = "Updates an existing community. Only community owner can update. Requires authentication."
    )
    fun updateCommunity(
        @PathVariable id: Long,
        @Valid @RequestBody request: UpdateCommunityRequest
    ): ApiResponse<CommunityDto> {
        logger.info { "PUT /api/v1/communities/$id" }
        
        val community = communityService.updateCommunity(id, request)
        
        return ApiResponse.success(
            data = community,
            message = "Community updated successfully"
        )
    }
    
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(
        summary = "Delete community",
        description = "Deletes a community. Only community owner can delete. Requires authentication."
    )
    fun deleteCommunity(@PathVariable id: Long): ApiResponse<Unit> {
        logger.info { "DELETE /api/v1/communities/$id" }
        
        communityService.deleteCommunity(id)
        
        return ApiResponse.success(
            data = Unit,
            message = "Community deleted successfully"
        )
    }
    
    @PostMapping("/{id}/subscribe")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(
        summary = "Subscribe to community",
        description = "Subscribes current user to the community. Requires authentication."
    )
    fun subscribeToCommunity(@PathVariable id: Long): ApiResponse<Unit> {
        logger.info { "POST /api/v1/communities/$id/subscribe" }
        
        communityService.subscribeToCommunity(id)
        
        return ApiResponse.success(
            data = Unit,
            message = "Successfully subscribed to community"
        )
    }
    
    @DeleteMapping("/{id}/subscribe")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(
        summary = "Unsubscribe from community",
        description = "Removes current user's subscription from the community. Requires authentication."
    )
    fun unsubscribeFromCommunity(@PathVariable id: Long): ApiResponse<Unit> {
        logger.info { "DELETE /api/v1/communities/$id/subscribe" }
        
        communityService.unsubscribeFromCommunity(id)
        
        return ApiResponse.success(
            data = Unit,
            message = "Successfully unsubscribed from community"
        )
    }
    
    @GetMapping("/{id}/meetings")
    @Operation(
        summary = "Get community meetings",
        description = "Returns all meetings hosted by this community"
    )
    fun getCommunityMeetings(
        @PathVariable id: Long,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int
    ): ApiResponse<PaginationResponse<MeetingDto>> {
        logger.info { "GET /api/v1/communities/$id/meetings - page: $page, size: $size" }
        
        val meetings = communityService.getCommunityMeetings(id, page, size)
        
        return ApiResponse.success(
            data = meetings.toPaginationResponse(),
            message = "Community meetings retrieved successfully"
        )
    }
    
    private fun <T> Page<T>.toPaginationResponse(): PaginationResponse<T> {
        return PaginationResponse(
            items = content,
            page = number,
            limit = size,
            total = totalElements,
            hasMore = hasNext()
        )
    }
}
