package dev.whysoezzy.meet.domain.entity

import jakarta.persistence.*

@Entity
@Table(name = "communities")
class Community(
    
    @Column(name = "image_url", nullable = false, columnDefinition = "TEXT")
    var imageUrl: String,
    
    @Column(nullable = false, length = 255)
    var title: String,
    
    @Column(nullable = false, columnDefinition = "TEXT")
    var description: String,
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", nullable = false)
    var owner: User,
    
    @ManyToMany
    @JoinTable(
        name = "community_tags",
        joinColumns = [JoinColumn(name = "community_id")],
        inverseJoinColumns = [JoinColumn(name = "tag_id")]
    )
    var tags: MutableSet<Tag> = mutableSetOf(),
    
    @ManyToMany
    @JoinTable(
        name = "community_subscribers",
        joinColumns = [JoinColumn(name = "community_id")],
        inverseJoinColumns = [JoinColumn(name = "user_id")]
    )
    var subscribers: MutableSet<User> = mutableSetOf(),
    
    @OneToMany(mappedBy = "communityHost")
    var meetings: MutableList<Meeting> = mutableListOf()
    
) : BaseEntity() {
    
    fun addTag(tag: Tag) {
        tags.add(tag)
    }
    
    fun removeTag(tag: Tag) {
        tags.remove(tag)
    }
    
    fun addSubscriber(user: User) {
        subscribers.add(user)
    }
    
    fun removeSubscriber(user: User) {
        subscribers.remove(user)
    }
    
    fun getActiveMeetings(): List<Meeting> {
        return meetings.filter { it.status == MeetingStatus.ACTIVE }
    }
}
