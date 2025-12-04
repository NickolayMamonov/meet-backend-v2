package dev.whysoezzy.meet.service

import dev.whysoezzy.meet.api.dto.common.TagDto
import dev.whysoezzy.meet.domain.entity.TagState
import dev.whysoezzy.meet.domain.repository.TagRepository
import mu.KotlinLogging
import org.springframework.cache.annotation.CacheEvict
import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

private val logger = KotlinLogging.logger {}

@Service
class TagService(
    private val tagRepository: TagRepository
) {

    /**
     * Get all active tags
     * Cached for 24 hours (configured in CacheConfig)
     */
    @Cacheable(value = ["tags"], key = "'all_active'")
    @Transactional(readOnly = true)
    fun getAllActiveTags(): List<TagDto> {
        logger.info { "Fetching all active tags from database (cache miss)" }

        return tagRepository.findAllByState(TagState.ACTIVE)
            .map { tag ->
                TagDto(
                    id = tag.id!!,
                    text = tag.text,
                    state = tag.state.name
                )
            }
    }

    /**
     * Clear tags cache
     * Use this when tags are modified in admin panel
     */
    @CacheEvict(value = ["tags"], allEntries = true)
    fun clearCache() {
        logger.info { "Clearing tags cache" }
    }
}