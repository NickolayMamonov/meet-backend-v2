package dev.whysoezzy.meet.service

import dev.whysoezzy.meet.api.dto.common.TagDto
import dev.whysoezzy.meet.domain.entity.TagState
import dev.whysoezzy.meet.domain.repository.TagRepository
import mu.KotlinLogging
import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

private val logger = KotlinLogging.logger {}

@Service
class TagService(
    private val tagRepository: TagRepository
) {
    
    @Cacheable("tags")
    @Transactional(readOnly = true)
    fun getAllActiveTags(): List<TagDto> {
        logger.debug { "Fetching all active tags" }
        
        return tagRepository.findAllByState(TagState.ACTIVE)
            .map { tag ->
                TagDto(
                    id = tag.id!!,
                    text = tag.text,
                    state = tag.state.name
                )
            }
    }
}
