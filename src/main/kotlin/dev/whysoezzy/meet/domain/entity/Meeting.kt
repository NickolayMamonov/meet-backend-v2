package dev.whysoezzy.meet.domain.entity

import jakarta.persistence.*

@Entity
@Table(name = "meetings")
class Meeting(
    
    @Column(name = "image_url", nullable = false, columnDefinition = "TEXT")
    var imageUrl: String,
    
    @Column(nullable = false, length = 255)
    var title: String,
    
    @Column(nullable = false, columnDefinition = "TEXT")
    var description: String,
    
    @Column(nullable = false)
    var time: Long,
    
    @Column(nullable = false, length = 50)
    var date: String,
    
    @Column(nullable = false, columnDefinition = "TEXT")
    var address: String,
    
    @Column(nullable = false)
    var latitude: Double,
    
    @Column(nullable = false)
    var longitude: Double,
    
    @Column(nullable = false)
    var capacity: Int = 100,
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    var status: MeetingStatus = MeetingStatus.ACTIVE,
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "person_host_id")
    var personHost: User? = null,
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "community_host_id")
    var communityHost: Community? = null,
    
    @ManyToMany
    @JoinTable(
        name = "meeting_tags",
        joinColumns = [JoinColumn(name = "meeting_id")],
        inverseJoinColumns = [JoinColumn(name = "tag_id")]
    )
    var tags: MutableSet<Tag> = mutableSetOf(),
    
    @ManyToMany
    @JoinTable(
        name = "meeting_participants",
        joinColumns = [JoinColumn(name = "meeting_id")],
        inverseJoinColumns = [JoinColumn(name = "user_id")]
    )
    var participants: MutableSet<User> = mutableSetOf()
    
) : BaseEntity() {
    
    fun addTag(tag: Tag) {
        tags.add(tag)
    }
    
    fun removeTag(tag: Tag) {
        tags.remove(tag)
    }
    
    fun addParticipant(user: User) {
        if (participants.size >= capacity) {
            throw IllegalStateException("Meeting is at full capacity")
        }
        participants.add(user)
    }
    
    fun removeParticipant(user: User) {
        participants.remove(user)
    }
    
    fun isFull(): Boolean = participants.size >= capacity
    
    fun isActive(): Boolean = status == MeetingStatus.ACTIVE
}

enum class MeetingStatus {
    ACTIVE,
    CANCELLED,
    FINISHED
}
