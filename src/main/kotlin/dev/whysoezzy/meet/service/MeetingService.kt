package dev.whysoezzy.meet.service

import dev.whysoezzy.meet.api.dto.common.AddressDto
import dev.whysoezzy.meet.api.dto.common.TagDto
import dev.whysoezzy.meet.api.dto.meeting.*
import dev.whysoezzy.meet.common.exception.BadRequestException
import dev.whysoezzy.meet.common.exception.ForbiddenException
import dev.whysoezzy.meet.common.exception.ResourceNotFoundException
import dev.whysoezzy.meet.domain.entity.Meeting
import dev.whysoezzy.meet.domain.entity.MeetingStatus
import dev.whysoezzy.meet.domain.repository.CommunityRepository
import dev.whysoezzy.meet.domain.repository.MeetingRepository
import dev.whysoezzy.meet.domain.repository.TagRepository
import dev.whysoezzy.meet.domain.repository.UserRepository
import dev.whysoezzy.meet.security.SecurityUtils
import mu.KotlinLogging
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant

private val logger = KotlinLogging.logger {}

@Service
class MeetingService(
    private val meetingRepository: MeetingRepository,
    private val userRepository: UserRepository,
    private val communityRepository: CommunityRepository,
    private val tagRepository: TagRepository
) {
    
    @Transactional(readOnly = true)
    fun getMainScreenData(): MainScreenDataDto {
        logger.info { "Fetching main screen data" }
        
        val currentUserId = SecurityUtils.getCurrentUserIdOrNull()
        val currentTime = Instant.now().epochSecond
        
        // Get hero meeting (nearest upcoming meeting with most participants)
        val heroMeeting = meetingRepository.findUpcomingMeetings(
            status = MeetingStatus.ACTIVE,
            currentTime = currentTime,
            pageable = PageRequest.of(0, 1, Sort.by(Sort.Direction.ASC, "time"))
        ).content.firstOrNull()?.toDto(currentUserId)
        
        // Get popular meetings (most participants)
        val popularMeetings = meetingRepository.findPopularMeetings(
            status = MeetingStatus.ACTIVE,
            pageable = PageRequest.of(0, 10)
        ).content.map { it.toDto(currentUserId) }
        
        // Get upcoming meetings (sorted by time)
        val upcomingMeetings = meetingRepository.findUpcomingMeetings(
            status = MeetingStatus.ACTIVE,
            currentTime = currentTime,
            pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.ASC, "time"))
        ).content.map { it.toDto(currentUserId) }
        
        return MainScreenDataDto(
            heroMeeting = heroMeeting,
            popularMeetings = popularMeetings,
            upcomingMeetings = upcomingMeetings
        )
    }
    
    @Transactional(readOnly = true)
    fun getAllMeetings(
        filters: MeetingFilters,
        page: Int,
        size: Int
    ): Page<MeetingDto> {
        logger.info { "Fetching all meetings with filters: $filters, page: $page, size: $size" }
        
        val currentUserId = SecurityUtils.getCurrentUserIdOrNull()
        val pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"))
        
        val status = MeetingStatus.valueOf(filters.status ?: "ACTIVE")
        
        return when {
            !filters.search.isNullOrBlank() -> {
                meetingRepository.searchMeetings(filters.search, status, pageable)
            }
            !filters.tagIds.isNullOrEmpty() -> {
                meetingRepository.findByTagsIn(filters.tagIds, status, pageable)
            }
            else -> {
                meetingRepository.findAllByStatus(status, pageable)
            }
        }.map { it.toDto(currentUserId) }
    }
    
    @Transactional(readOnly = true)
    fun getMeetingById(id: Long): MeetingDetailDto {
        logger.info { "Fetching meeting by id: $id" }
        
        val currentUserId = SecurityUtils.getCurrentUserIdOrNull()
        val meeting = meetingRepository.findByIdWithDetails(id)
            ?: throw ResourceNotFoundException("Meeting not found with id: $id")
        
        return meeting.toDetailDto(currentUserId)
    }
    
    @Transactional
    fun createMeeting(request: CreateMeetingRequest): MeetingDto {
        val currentUserId = SecurityUtils.getCurrentUserId()
        logger.info { "Creating meeting by user: $currentUserId" }
        
        val user = userRepository.findById(currentUserId).orElseThrow {
            ResourceNotFoundException("User not found")
        }
        
        // Validate time is in future
        if (request.time < Instant.now().epochSecond) {
            throw BadRequestException("Meeting time must be in the future")
        }
        
        val meeting = Meeting(
            imageUrl = request.imageUrl,
            title = request.title,
            description = request.description,
            time = request.time,
            date = request.date,
            address = request.address,
            latitude = request.latitude,
            longitude = request.longitude,
            capacity = request.capacity,
            personHost = if (request.communityHostId == null) user else null,
            communityHost = request.communityHostId?.let {
                communityRepository.findById(it).orElseThrow {
                    ResourceNotFoundException("Community not found")
                }
            }
        )
        
        // Add tags
        if (!request.tagIds.isNullOrEmpty()) {
            val tags = tagRepository.findAllById(request.tagIds)
            tags.forEach { meeting.addTag(it) }
        }
        
        val savedMeeting = meetingRepository.save(meeting)
        
        logger.info { "Meeting created successfully with id: ${savedMeeting.id}" }
        
        return savedMeeting.toDto(currentUserId)
    }
    
    @Transactional
    fun updateMeeting(id: Long, request: UpdateMeetingRequest): MeetingDto {
        val currentUserId = SecurityUtils.getCurrentUserId()
        logger.info { "Updating meeting id: $id by user: $currentUserId" }
        
        val meeting = meetingRepository.findById(id).orElseThrow {
            ResourceNotFoundException("Meeting not found with id: $id")
        }
        
        // Check if user is host
        if (meeting.personHost?.id != currentUserId) {
            throw ForbiddenException("Only meeting host can update the meeting")
        }
        
        // Update fields
        request.imageUrl?.let { meeting.imageUrl = it }
        request.title?.let { meeting.title = it }
        request.description?.let { meeting.description = it }
        request.time?.let {
            if (it < Instant.now().epochSecond) {
                throw BadRequestException("Meeting time must be in the future")
            }
            meeting.time = it
        }
        request.date?.let { meeting.date = it }
        request.address?.let { meeting.address = it }
        request.latitude?.let { meeting.latitude = it }
        request.longitude?.let { meeting.longitude = it }
        request.capacity?.let {
            if (it < meeting.participants.size) {
                throw BadRequestException("Capacity cannot be less than current participants count")
            }
            meeting.capacity = it
        }
        
        // Update tags
        request.tagIds?.let { tagIds ->
            meeting.tags.clear()
            val tags = tagRepository.findAllById(tagIds)
            tags.forEach { meeting.addTag(it) }
        }
        
        val updatedMeeting = meetingRepository.save(meeting)
        
        logger.info { "Meeting updated successfully: $id" }
        
        return updatedMeeting.toDto(currentUserId)
    }
    
    @Transactional
    fun deleteMeeting(id: Long) {
        val currentUserId = SecurityUtils.getCurrentUserId()
        logger.info { "Deleting meeting id: $id by user: $currentUserId" }
        
        val meeting = meetingRepository.findById(id).orElseThrow {
            ResourceNotFoundException("Meeting not found with id: $id")
        }
        
        // Check if user is host
        if (meeting.personHost?.id != currentUserId) {
            throw ForbiddenException("Only meeting host can delete the meeting")
        }
        
        meetingRepository.delete(meeting)
        
        logger.info { "Meeting deleted successfully: $id" }
    }
    
    @Transactional
    fun registerForMeeting(id: Long) {
        val currentUserId = SecurityUtils.getCurrentUserId()
        logger.info { "User $currentUserId registering for meeting: $id" }
        
        val meeting = meetingRepository.findById(id).orElseThrow {
            ResourceNotFoundException("Meeting not found with id: $id")
        }
        
        val user = userRepository.findById(currentUserId).orElseThrow {
            ResourceNotFoundException("User not found")
        }
        
        // Check if meeting is active
        if (!meeting.isActive()) {
            throw BadRequestException("Cannot register for inactive meeting")
        }
        
        // Check if already registered
        if (meetingRepository.isUserParticipant(id, currentUserId)) {
            throw BadRequestException("User is already registered for this meeting")
        }
        
        // Check if meeting is full
        if (meeting.isFull()) {
            throw BadRequestException("Meeting is at full capacity")
        }
        
        meeting.addParticipant(user)
        meetingRepository.save(meeting)
        
        logger.info { "User $currentUserId registered successfully for meeting: $id" }
    }
    
    @Transactional
    fun unregisterFromMeeting(id: Long) {
        val currentUserId = SecurityUtils.getCurrentUserId()
        logger.info { "User $currentUserId unregistering from meeting: $id" }
        
        val meeting = meetingRepository.findById(id).orElseThrow {
            ResourceNotFoundException("Meeting not found with id: $id")
        }
        
        val user = userRepository.findById(currentUserId).orElseThrow {
            ResourceNotFoundException("User not found")
        }
        
        // Check if user is registered
        if (!meetingRepository.isUserParticipant(id, currentUserId)) {
            throw BadRequestException("User is not registered for this meeting")
        }
        
        meeting.removeParticipant(user)
        meetingRepository.save(meeting)
        
        logger.info { "User $currentUserId unregistered successfully from meeting: $id" }
    }
    
    @Transactional(readOnly = true)
    fun getUserMeetings(page: Int, size: Int): Page<MeetingDto> {
        val currentUserId = SecurityUtils.getCurrentUserId()
        logger.info { "Fetching meetings for user: $currentUserId" }
        
        val pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "time"))
        
        return meetingRepository.findByParticipantId(currentUserId, pageable)
            .map { it.toDto(currentUserId) }
    }
    
    // Extension functions for mapping
    
    private fun Meeting.toDto(currentUserId: Long?): MeetingDto {
        return MeetingDto(
            id = id!!,
            imageUrl = imageUrl,
            title = title,
            description = description,
            time = time,
            date = date,
            address = AddressDto(address, latitude, longitude),
            capacity = capacity,
            participantsCount = participants.size,
            status = status.name,
            tags = tags.map { TagDto(it.id!!, it.text, it.state.name) },
            host = if (personHost != null) {
                HostDto(personHost!!.id!!, "${personHost!!.name} ${personHost!!.surname}", "USER", personHost!!.imageUrl)
            } else {
                HostDto(communityHost!!.id!!, communityHost!!.title, "COMMUNITY", communityHost!!.imageUrl)
            },
            isRegistered = currentUserId?.let { participants.any { p -> p.id == it } } ?: false
        )
    }
    
    private fun Meeting.toDetailDto(currentUserId: Long?): MeetingDetailDto {
        return MeetingDetailDto(
            id = id!!,
            imageUrl = imageUrl,
            title = title,
            description = description,
            time = time,
            date = date,
            address = AddressDto(address, latitude, longitude),
            capacity = capacity,
            participantsCount = participants.size,
            status = status.name,
            tags = tags.map { TagDto(it.id!!, it.text, it.state.name) },
            host = if (personHost != null) {
                HostDto(personHost!!.id!!, "${personHost!!.name} ${personHost!!.surname}", "USER", personHost!!.imageUrl)
            } else {
                HostDto(communityHost!!.id!!, communityHost!!.title, "COMMUNITY", communityHost!!.imageUrl)
            },
            participants = participants.map {
                ParticipantDto(it.id!!, it.name, it.surname, it.imageUrl)
            },
            isRegistered = currentUserId?.let { participants.any { p -> p.id == it } } ?: false,
            isFull = isFull()
        )
    }
}
