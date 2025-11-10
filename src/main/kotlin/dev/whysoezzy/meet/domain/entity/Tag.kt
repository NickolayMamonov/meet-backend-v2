package dev.whysoezzy.meet.domain.entity

import jakarta.persistence.*

@Entity
@Table(name = "tags")
class Tag(
    
    @Column(nullable = false, unique = true, length = 50)
    var text: String,
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    var state: TagState = TagState.ACTIVE,
    
    @ManyToMany(mappedBy = "interests")
    var users: MutableSet<User> = mutableSetOf(),
    
    @ManyToMany(mappedBy = "tags")
    var communities: MutableSet<Community> = mutableSetOf(),
    
    @ManyToMany(mappedBy = "tags")
    var meetings: MutableSet<Meeting> = mutableSetOf()
    
) : BaseEntity()

enum class TagState {
    ACTIVE,
    INACTIVE
}
