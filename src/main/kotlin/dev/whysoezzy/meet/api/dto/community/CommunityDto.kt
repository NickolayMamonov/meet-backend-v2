package dev.whysoezzy.meet.api.dto.community

import dev.whysoezzy.meet.api.dto.common.TagDto
import dev.whysoezzy.meet.api.dto.meeting.MeetingDto
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

// Response DTOs

data class CommunityDto(
    val id: Long,
    val imageUrl: String,
    val title: String,
    val description: String,
    val subscribersCount: Int,
    val tags: List<TagDto>,
    val isSubscribed: Boolean = false
)

data class CommunityDetailDto(
    val id: Long,
    val imageUrl: String,
    val title: String,
    val description: String,
    val subscribersCount: Int,
    val tags: List<TagDto>,
    val upcomingMeetings: List<MeetingDto>,
    val isSubscribed: Boolean = false
)

// Request DTOs

data class CreateCommunityRequest(
    @field:NotBlank(message = "Image URL is required")
    val imageUrl: String,
    
    @field:NotBlank(message = "Title is required")
    @field:Size(min = 3, max = 255, message = "Title must be between 3 and 255 characters")
    val title: String,
    
    @field:NotBlank(message = "Description is required")
    @field:Size(min = 10, message = "Description must be at least 10 characters")
    val description: String,
    
    val tagIds: List<Long>? = null
)

data class UpdateCommunityRequest(
    val imageUrl: String? = null,
    
    @field:Size(min = 3, max = 255, message = "Title must be between 3 and 255 characters")
    val title: String? = null,
    
    @field:Size(min = 10, message = "Description must be at least 10 characters")
    val description: String? = null,
    
    val tagIds: List<Long>? = null
)
