package dev.whysoezzy.meet.domain.repository

import dev.whysoezzy.meet.domain.entity.Tag
import dev.whysoezzy.meet.domain.entity.TagState
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface TagRepository : JpaRepository<Tag, Long> {
    
    fun findByText(text: String): Tag?
    
    fun findAllByState(state: TagState): List<Tag>
    
    fun existsByText(text: String): Boolean
}
