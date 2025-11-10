package dev.whysoezzy.meet.domain.repository

import dev.whysoezzy.meet.domain.entity.Community
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

@Repository
interface CommunityRepository : JpaRepository<Community, Long> {
    
    @Query("""
        SELECT c FROM Community c
        LEFT JOIN FETCH c.tags
        WHERE c.id = :id
    """)
    fun findByIdWithDetails(@Param("id") id: Long): Community?
    
    @Query("""
        SELECT c FROM Community c
        LEFT JOIN c.subscribers s
        GROUP BY c.id
        ORDER BY COUNT(s) DESC
    """)
    fun findPopularCommunities(pageable: Pageable): Page<Community>
    
    @Query("""
        SELECT c FROM Community c
        JOIN c.tags t
        WHERE t.id IN :tagIds
    """)
    fun findByTagsIn(
        @Param("tagIds") tagIds: List<Long>,
        pageable: Pageable
    ): Page<Community>
    
    @Query("""
        SELECT c FROM Community c
        WHERE LOWER(c.title) LIKE LOWER(CONCAT('%', :query, '%'))
        OR LOWER(c.description) LIKE LOWER(CONCAT('%', :query, '%'))
    """)
    fun searchCommunities(
        @Param("query") query: String,
        pageable: Pageable
    ): Page<Community>
    
    @Query("""
        SELECT CASE WHEN COUNT(s) > 0 THEN true ELSE false END
        FROM Community c
        JOIN c.subscribers s
        WHERE c.id = :communityId AND s.id = :userId
    """)
    fun isUserSubscribed(
        @Param("communityId") communityId: Long,
        @Param("userId") userId: Long
    ): Boolean
}
