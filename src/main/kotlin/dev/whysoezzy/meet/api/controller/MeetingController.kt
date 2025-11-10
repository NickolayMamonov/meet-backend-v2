package dev.whysoezzy.meet.api.controller

import dev.whysoezzy.meet.api.dto.common.ApiResponse
import dev.whysoezzy.meet.api.dto.common.PaginationResponse
import dev.whysoezzy.meet.api.dto.meeting.*
import dev.whysoezzy.meet.service.MeetingService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import jakarta.validation.Valid
import mu.KotlinLogging
import org.springframework.data.domain.Page
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*

private val logger = KotlinLogging.logger {}

@RestController
@RequestMapping("/api/v1/meetings")
@io.swagger.v3.oas.annotations.tags.Tag(name = "Meetings", description = "Meeting management endpoints")
class MeetingController(
    private val meetingService: MeetingService
) {
    
    @GetMapping("/main")
    @Operation(
        summary = "Get main screen data",
        description = "Returns hero meeting, popular meetings and upcoming meetings for the main screen"
    )
    fun getMainScreenData(): ApiResponse<MainScreenDataDto> {
        logger.info { "GET /api/v1/meetings/main" }
        
        val data = meetingService.getMainScreenData()
        
        return ApiResponse.success(
            data = data,
            message = "Main screen data retrieved successfully"
        )
    }
    
    @GetMapping
    @Operation(
        summary = "Get all meetings",
        description = "Returns paginated list of meetings with optional filters"
    )
    fun getAllMeetings(
        @RequestParam(required = false) tagIds: List<Long>?,
        @RequestParam(required = false) city: String?,
        @RequestParam(required = false) fromDate: Long?,
        @RequestParam(required = false) toDate: Long?,
        @RequestParam(required = false) search: String?,
        @RequestParam(required = false, defaultValue = "ACTIVE") status: String?,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int
    ): ApiResponse<PaginationResponse<MeetingDto>> {
        logger.info { "GET /api/v1/meetings - page: $page, size: $size" }
        
        val filters = MeetingFilters(tagIds, city, fromDate, toDate, search, status)
        val meetings = meetingService.getAllMeetings(filters, page, size)
        
        return ApiResponse.success(
            data = meetings.toPaginationResponse(),
            message = "Meetings retrieved successfully"
        )
    }
    
    @GetMapping("/{id}")
    @Operation(
        summary = "Get meeting by ID",
        description = "Returns detailed information about a specific meeting"
    )
    fun getMeetingById(@PathVariable id: Long): ApiResponse<MeetingDetailDto> {
        logger.info { "GET /api/v1/meetings/$id" }
        
        val meeting = meetingService.getMeetingById(id)
        
        return ApiResponse.success(
            data = meeting,
            message = "Meeting retrieved successfully"
        )
    }
    
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(
        summary = "Create meeting",
        description = "Creates a new meeting. Requires authentication."
    )
    fun createMeeting(
        @Valid @RequestBody request: CreateMeetingRequest
    ): ApiResponse<MeetingDto> {
        logger.info { "POST /api/v1/meetings - title: ${request.title}" }
        
        val meeting = meetingService.createMeeting(request)
        
        return ApiResponse.success(
            data = meeting,
            message = "Meeting created successfully"
        )
    }
    
    @PutMapping("/{id}")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(
        summary = "Update meeting",
        description = "Updates an existing meeting. Only meeting host can update. Requires authentication."
    )
    fun updateMeeting(
        @PathVariable id: Long,
        @Valid @RequestBody request: UpdateMeetingRequest
    ): ApiResponse<MeetingDto> {
        logger.info { "PUT /api/v1/meetings/$id" }
        
        val meeting = meetingService.updateMeeting(id, request)
        
        return ApiResponse.success(
            data = meeting,
            message = "Meeting updated successfully"
        )
    }
    
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(
        summary = "Delete meeting",
        description = "Deletes a meeting. Only meeting host can delete. Requires authentication."
    )
    fun deleteMeeting(@PathVariable id: Long): ApiResponse<Unit> {
        logger.info { "DELETE /api/v1/meetings/$id" }
        
        meetingService.deleteMeeting(id)
        
        return ApiResponse.success(
            data = Unit,
            message = "Meeting deleted successfully"
        )
    }
    
    @PostMapping("/{id}/register")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(
        summary = "Register for meeting",
        description = "Registers current user as a participant for the meeting. Requires authentication."
    )
    fun registerForMeeting(@PathVariable id: Long): ApiResponse<Unit> {
        logger.info { "POST /api/v1/meetings/$id/register" }
        
        meetingService.registerForMeeting(id)
        
        return ApiResponse.success(
            data = Unit,
            message = "Successfully registered for meeting"
        )
    }
    
    @DeleteMapping("/{id}/register")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(
        summary = "Unregister from meeting",
        description = "Removes current user from meeting participants. Requires authentication."
    )
    fun unregisterFromMeeting(@PathVariable id: Long): ApiResponse<Unit> {
        logger.info { "DELETE /api/v1/meetings/$id/register" }
        
        meetingService.unregisterFromMeeting(id)
        
        return ApiResponse.success(
            data = Unit,
            message = "Successfully unregistered from meeting"
        )
    }
    
    @GetMapping("/my")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(
        summary = "Get user's meetings",
        description = "Returns all meetings that the current user is registered for. Requires authentication."
    )
    fun getUserMeetings(
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int
    ): ApiResponse<PaginationResponse<MeetingDto>> {
        logger.info { "GET /api/v1/meetings/my - page: $page, size: $size" }
        
        val meetings = meetingService.getUserMeetings(page, size)
        
        return ApiResponse.success(
            data = meetings.toPaginationResponse(),
            message = "User meetings retrieved successfully"
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
