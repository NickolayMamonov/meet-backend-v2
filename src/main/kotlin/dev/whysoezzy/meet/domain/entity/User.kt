package dev.whysoezzy.meet.domain.entity

import jakarta.persistence.*

@Entity
@Table(name = "users")
class User(
    
    @Column(nullable = false, length = 100)
    var name: String,
    
    @Column(nullable = false, length = 100)
    var surname: String,
    
    @Column(name = "phone_number", nullable = false, unique = true, length = 20)
    var phoneNumber: String,
    
    @Column(name = "image_url")
    var imageUrl: String? = null,
    
    @Column(length = 100)
    var city: String = "",
    
    @Column(columnDefinition = "TEXT")
    var description: String = "",
    
    @Column(length = 255)
    var email: String? = null,
    
    @ManyToMany
    @JoinTable(
        name = "user_interests",
        joinColumns = [JoinColumn(name = "user_id")],
        inverseJoinColumns = [JoinColumn(name = "tag_id")]
    )
    var interests: MutableSet<Tag> = mutableSetOf(),
    
    @OneToMany(mappedBy = "user", cascade = [CascadeType.ALL], orphanRemoval = true)
    var socialMedias: MutableList<UserSocialMedia> = mutableListOf(),
    
    @ManyToMany(mappedBy = "subscribers")
    var subscribedCommunities: MutableSet<Community> = mutableSetOf(),
    
    @ManyToMany(mappedBy = "participants")
    var registeredMeetings: MutableSet<Meeting> = mutableSetOf()
    
) : BaseEntity() {
    
    fun addInterest(tag: Tag) {
        interests.add(tag)
    }
    
    fun removeInterest(tag: Tag) {
        interests.remove(tag)
    }
    
    fun addSocialMedia(platform: SocialMediaPlatform, username: String) {
        socialMedias.add(UserSocialMedia(user = this, platform = platform, username = username))
    }
    
    fun subscribeToCommunity(community: Community) {
        subscribedCommunities.add(community)
        community.subscribers.add(this)
    }
    
    fun unsubscribeFromCommunity(community: Community) {
        subscribedCommunities.remove(community)
        community.subscribers.remove(this)
    }
    
    fun registerForMeeting(meeting: Meeting) {
        registeredMeetings.add(meeting)
        meeting.participants.add(this)
    }
    
    fun unregisterFromMeeting(meeting: Meeting) {
        registeredMeetings.remove(meeting)
        meeting.participants.remove(this)
    }
}

@Entity
@Table(name = "user_social_media", uniqueConstraints = [
    UniqueConstraint(columnNames = ["user_id", "platform"])
])
class UserSocialMedia(
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    var user: User,
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    var platform: SocialMediaPlatform,
    
    @Column(nullable = false, length = 255)
    var username: String
    
) : BaseEntity()

enum class SocialMediaPlatform {
    TELEGRAM,
    HABR
}
