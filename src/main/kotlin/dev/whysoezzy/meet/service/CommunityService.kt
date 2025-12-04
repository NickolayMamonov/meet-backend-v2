package dev.whysoezzy.meet.service

import dev.whysoezzy.meet.api.dto.common.AddressDto
import dev.whysoezzy.meet.api.dto.common.TagDto
import dev.whysoezzy.meet.api.dto.community.*
import dev.whysoezzy.meet.api.dto.meeting.HostDto
import dev.whysoezzy.meet.api.dto.meeting.MeetingDto
import dev.whysoezzy.meet.common.exception.BadRequestException
import dev.whysoezzy.meet.common.exception.ForbiddenException
import dev.whysoezzy.meet.common.exception.ResourceNotFoundException
import dev.whysoezzy.meet.domain.entity.Community
import dev.whysoezzy.meet.domain.entity.Meeting
import dev.whysoezzy.meet.domain.entity.MeetingStatus
import dev.whysoezzy.meet.domain.repository.CommunityRepository
import dev.whysoezzy.meet.domain.repository.MeetingRepository
import dev.whysoezzy.meet.domain.repository.TagRepository
import dev.whysoezzy.meet.domain.repository.UserRepository
import dev.whysoezzy.meet.security.SecurityUtils
import mu.KotlinLogging
import org.springframework.cache.annotation.CacheEvict
import org.springframework.cache.annotation.Cacheable
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant

private val logger = KotlinLogging.logger {}

@Service
class CommunityService(
    private val communityRepository: CommunityRepository,
    private val userRepository: UserRepository,
    private val tagRepository: TagRepository,
    private val meetingRepository: MeetingRepository
) {

    @Transactional(readOnly = true)
    fun getAllCommunities(page: Int, size: Int): Page<CommunityDto> {
        logger.info { "Fetching all communities - page: $page, size: $size" }

        val currentUserId = SecurityUtils.getCurrentUserIdOrNull()
        val pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"))

        return communityRepository.findAll(pageable)
            .map { it.toDto(currentUserId) }
    }

    /**
     * Get community by ID
     * Cached for 10 minutes
     */
    @Cacheable(
        value = ["community-detail"],
        key = "#id",
        unless = "#result == null"
    )
    @Transactional(readOnly = true)
    fun getCommunityById(id: Long): CommunityDetailDto {
        logger.info { "Fetching community by id: $id from database (cache miss)" }

        val currentUserId = SecurityUtils.getCurrentUserIdOrNull()
        val community = communityRepository.findById(id).orElseThrow {
            ResourceNotFoundException("Community not found with id: $id")
        }

        return community.toDetailDto(currentUserId)
    }

    /**
     * Create community
     * No cache eviction needed (doesn't affect existing caches)
     */
    @Transactional
    fun createCommunity(request: CreateCommunityRequest): CommunityDto {
        val currentUserId = SecurityUtils.getCurrentUserId()
        logger.info { "Creating community by user: $currentUserId" }

        val user = userRepository.findById(currentUserId).orElseThrow {
            ResourceNotFoundException("User not found")
        }

        val community = Community(
            imageUrl = request.imageUrl,
            title = request.title,
            description = request.description,
            owner = user
        )

        // Add tags
        if (!request.tagIds.isNullOrEmpty()) {
            val tags = tagRepository.findAllById(request.tagIds)
            tags.forEach { community.addTag(it) }
        }

        val savedCommunity = communityRepository.save(community)

        logger.info { "Community created successfully with id: ${savedCommunity.id}" }

        return savedCommunity.toDto(currentUserId)
    }

    /**
     * Update community
     * Evicts community-detail cache
     */
    @CacheEvict(value = ["community-detail"], key = "#id")
    @Transactional
    fun updateCommunity(id: Long, request: UpdateCommunityRequest): CommunityDto {
        val currentUserId = SecurityUtils.getCurrentUserId()
        logger.info { "Updating community id: $id by user: $currentUserId" }

        val community = communityRepository.findById(id).orElseThrow {
            ResourceNotFoundException("Community not found with id: $id")
        }

        // Check if user is owner
        if (community.owner.id != currentUserId) {
            throw ForbiddenException("Only community owner can update the community")
        }

        // Update fields
        request.imageUrl?.let { community.imageUrl = it }
        request.title?.let { community.title = it }
        request.description?.let { community.description = it }

        // Update tags
        request.tagIds?.let { tagIds ->
            community.tags.clear()
            val tags = tagRepository.findAllById(tagIds)
            tags.forEach { community.addTag(it) }
        }

        val updatedCommunity = communityRepository.save(community)

        logger.info { "Community updated successfully: $id" }

        return updatedCommunity.toDto(currentUserId)
    }

    /**
     * Delete community
     * Evicts community-detail cache
     */
    @CacheEvict(value = ["community-detail"], key = "#id")
    @Transactional
    fun deleteCommunity(id: Long) {
        val currentUserId = SecurityUtils.getCurrentUserId()
        logger.info { "Deleting community id: $id by user: $currentUserId" }

        val community = communityRepository.findById(id).orElseThrow {
            ResourceNotFoundException("Community not found with id: $id")
        }

        // Check if user is owner
        if (community.owner.id != currentUserId) {
            throw ForbiddenException("Only community owner can delete the community")
        }

        communityRepository.delete(community)

        logger.info { "Community deleted successfully: $id" }
    }

    /**
     * Subscribe to community
     * Evicts community-detail cache
     */
    @CacheEvict(value = ["community-detail"], key = "#id")
    @Transactional
    fun subscribeToCommunity(id: Long) {
        val currentUserId = SecurityUtils.getCurrentUserId()
        logger.info { "User $currentUserId subscribing to community: $id" }

        val community = communityRepository.findById(id).orElseThrow {
            ResourceNotFoundException("Community not found with id: $id")
        }

        val user = userRepository.findById(currentUserId).orElseThrow {
            ResourceNotFoundException("User not found")
        }

        // Check if already subscribed
        if (community.subscribers.contains(user)) {
            throw BadRequestException("User is already subscribed to this community")
        }

        community.addSubscriber(user)
        communityRepository.save(community)

        logger.info { "User $currentUserId subscribed successfully to community: $id" }
    }

    /**
     * Unsubscribe from community
     * Evicts community-detail cache
     */
    @CacheEvict(value = ["community-detail"], key = "#id")
    @Transactional
    fun unsubscribeFromCommunity(id: Long) {
        val currentUserId = SecurityUtils.getCurrentUserId()
        logger.info { "User $currentUserId unsubscribing from community: $id" }

        val community = communityRepository.findById(id).orElseThrow {
            ResourceNotFoundException("Community not found with id: $id")
        }

        val user = userRepository.findById(currentUserId).orElseThrow {
            ResourceNotFoundException("User not found")
        }

        // Check if subscribed
        if (!community.subscribers.contains(user)) {
            throw BadRequestException("User is not subscribed to this community")
        }

        community.removeSubscriber(user)
        communityRepository.save(community)

        logger.info { "User $currentUserId unsubscribed successfully from community: $id" }
    }

    @Transactional(readOnly = true)
    fun getCommunityMeetings(id: Long, page: Int, size: Int): Page<MeetingDto> {
        logger.info { "Fetching meetings for community: $id" }

        val currentUserId = SecurityUtils.getCurrentUserIdOrNull()

        val community = communityRepository.findById(id).orElseThrow {
            ResourceNotFoundException("Community not found with id: $id")
        }

        val pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "time"))

        return meetingRepository.findByCommunityHost(community, pageable)
            .map { it.toMeetingDto(currentUserId) }
    }

    // Extension functions for mapping

    private fun Community.toDto(currentUserId: Long?): CommunityDto {
        return CommunityDto(
            id = id!!,
            imageUrl = imageUrl,
            title = title,
            description = description,
            subscribersCount = subscribers.size,
            tags = tags.map { TagDto(it.id!!, it.text, it.state.name) },
            isSubscribed = currentUserId?.let { subscribers.any { s -> s.id == it } } ?: false
        )
    }

    private fun Community.toDetailDto(currentUserId: Long?): CommunityDetailDto {
        val currentTime = Instant.now().epochSecond
        val upcomingMeetings = meetingRepository.findByCommunityHostAndStatusAndTimeGreaterThanEqual(
            this,
            MeetingStatus.ACTIVE,
            currentTime,
            PageRequest.of(0, 5, Sort.by(Sort.Direction.ASC, "time"))
        ).content.map { it.toMeetingDto(currentUserId) }

        return CommunityDetailDto(
            id = id!!,
            imageUrl = imageUrl,
            title = title,
            description = description,
            subscribersCount = subscribers.size,
            tags = tags.map { TagDto(it.id!!, it.text, it.state.name) },
            upcomingMeetings = upcomingMeetings,
            isSubscribed = currentUserId?.let { subscribers.any { s -> s.id == it } } ?: false
        )
    }

    private fun Meeting.toMeetingDto(currentUserId: Long?): MeetingDto {
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
                HostDto(
                    personHost!!.id!!,
                    "${personHost!!.name} ${personHost!!.surname}",
                    "USER",
                    personHost!!.imageUrl
                )
            } else {
                HostDto(communityHost!!.id!!, communityHost!!.title, "COMMUNITY", communityHost!!.imageUrl)
            },
            isRegistered = currentUserId?.let { participants.any { p -> p.id == it } } ?: false
        )
    }
}






