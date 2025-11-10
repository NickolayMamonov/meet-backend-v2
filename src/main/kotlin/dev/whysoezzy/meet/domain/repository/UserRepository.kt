package dev.whysoezzy.meet.domain.repository

import dev.whysoezzy.meet.domain.entity.User
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

@Repository
interface UserRepository : JpaRepository<User, Long> {
    
    fun findByPhoneNumber(phoneNumber: String): User?
    
    fun existsByPhoneNumber(phoneNumber: String): Boolean
    
    fun findByEmail(email: String): User?
    
    @Query("""
        SELECT u FROM User u 
        LEFT JOIN FETCH u.interests 
        LEFT JOIN FETCH u.socialMedias 
        WHERE u.id = :id
    """)
    fun findByIdWithDetails(@Param("id") id: Long): User?
    
    @Query("""
        SELECT DISTINCT u FROM User u
        LEFT JOIN FETCH u.interests
        WHERE u.id IN :ids
    """)
    fun findAllByIdWithInterests(@Param("ids") ids: List<Long>): List<User>
}
