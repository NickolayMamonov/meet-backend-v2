package dev.whysoezzy.meet.api.dto.meeting

import dev.whysoezzy.meet.api.dto.common.AddressDto
import dev.whysoezzy.meet.api.dto.common.TagDto
import jakarta.validation.constraints.*

// Response DTOs

data class MeetingDto(
    val id: Long,
    val imageUrl: String,
    val title: String,
    val description: String,
    val time: Long,
    val date: String,
    val address: AddressDto,
    val capacity: Int,
    val participantsCount: Int,
    val status: String,
    val tags: List<TagDto>,
    val host: HostDto,
    val isRegistered: Boolean = false
)

data class MeetingDetailDto(
    val id: Long,
    val imageUrl: String,
    val title: String,
    val description: String,
    val time: Long,
    val date: String,
    val address: AddressDto,
    val capacity: Int,
    val participantsCount: Int,
    val status: String,
    val tags: List<TagDto>,
    val host: HostDto,
    val participants: List<ParticipantDto>,
    val isRegistered: Boolean = false,
    val isFull: Boolean = false
)

data class HostDto(
    val id: Long,
    val name: String,
    val type: String, // "USER" or "COMMUNITY"
    val imageUrl: String?
)

data class ParticipantDto(
    val id: Long,
    val name: String,
    val surname: String,
    val imageUrl: String?
)

data class MainScreenDataDto(
    val heroMeeting: MeetingDto?,
    val popularMeetings: List<MeetingDto>,
    val upcomingMeetings: List<MeetingDto>
)

// Request DTOs

data class CreateMeetingRequest(
    @field:NotBlank(message = "Image URL is required")
    val imageUrl: String,
    
    @field:NotBlank(message = "Title is required")
    @field:Size(min = 3, max = 255, message = "Title must be between 3 and 255 characters")
    val title: String,
    
    @field:NotBlank(message = "Description is required")
    @field:Size(min = 10, message = "Description must be at least 10 characters")
    val description: String,
    
    @field:NotNull(message = "Time is required")
    @field:Min(value = 0, message = "Time must be positive")
    val time: Long,
    
    @field:NotBlank(message = "Date is required")
    val date: String,
    
    @field:NotBlank(message = "Address is required")
    val address: String,
    
    @field:NotNull(message = "Latitude is required")
    @field:Min(value = -90, message = "Latitude must be >= -90")
    @field:Max(value = 90, message = "Latitude must be <= 90")
    val latitude: Double,
    
    @field:NotNull(message = "Longitude is required")
    @field:Min(value = -180, message = "Longitude must be >= -180")
    @field:Max(value = 180, message = "Longitude must be <= 180")
    val longitude: Double,
    
    @field:NotNull(message = "Capacity is required")
    @field:Min(value = 1, message = "Capacity must be at least 1")
    @field:Max(value = 10000, message = "Capacity cannot exceed 10000")
    val capacity: Int,
    
    val tagIds: List<Long>? = null,
    
    val communityHostId: Long? = null
)

data class UpdateMeetingRequest(
    val imageUrl: String? = null,
    
    @field:Size(min = 3, max = 255, message = "Title must be between 3 and 255 characters")
    val title: String? = null,
    
    @field:Size(min = 10, message = "Description must be at least 10 characters")
    val description: String? = null,
    
    @field:Min(value = 0, message = "Time must be positive")
    val time: Long? = null,
    
    val date: String? = null,
    
    val address: String? = null,
    
    @field:Min(value = -90, message = "Latitude must be >= -90")
    @field:Max(value = 90, message = "Latitude must be <= 90")
    val latitude: Double? = null,
    
    @field:Min(value = -180, message = "Longitude must be >= -180")
    @field:Max(value = 180, message = "Longitude must be <= 180")
    val longitude: Double? = null,
    
    @field:Min(value = 1, message = "Capacity must be at least 1")
    @field:Max(value = 10000, message = "Capacity cannot exceed 10000")
    val capacity: Int? = null,
    
    val tagIds: List<Long>? = null
)

data class MeetingFilters(
    val tagIds: List<Long>? = null,
    val city: String? = null,
    val fromDate: Long? = null,
    val toDate: Long? = null,
    val search: String? = null,
    val status: String? = "ACTIVE"
)
