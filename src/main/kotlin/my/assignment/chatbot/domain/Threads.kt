package my.assignment.chatbot.domain

import jakarta.persistence.CascadeType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.OneToMany
import jakarta.persistence.Table
import org.hibernate.annotations.CreationTimestamp
import java.time.OffsetDateTime

@Entity
@Table(name = "threads")
class Threads(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    val user: User,

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    val createdAt: OffsetDateTime = OffsetDateTime.now(),

    @Column(name = "last_activity_at", nullable = false)
    var lastActivityAt: OffsetDateTime = OffsetDateTime.now()
) {
    @OneToMany(mappedBy = "threads", cascade = [CascadeType.ALL], fetch = FetchType.LAZY)
    val chats: MutableList<Chat> = mutableListOf()

    fun updateLastActivity() {
        lastActivityAt = OffsetDateTime.now()
    }

    fun isExpired(): Boolean {
        return OffsetDateTime.now().isAfter(lastActivityAt.plusMinutes(30))
    }
}
