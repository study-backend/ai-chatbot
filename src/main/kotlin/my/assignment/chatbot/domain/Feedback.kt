package my.assignment.chatbot.domain

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import org.hibernate.annotations.CreationTimestamp
import java.time.OffsetDateTime

@Entity
@Table(name = "feedback")
class Feedback (
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    val user: User,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chat_id", nullable = false)
    val chat: Chat,

    @Column(name = "is_positive", nullable = false)
    val isPositive: Boolean,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var status: FeedbackStatus = FeedbackStatus.PENDING,

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    val createdAt: OffsetDateTime = OffsetDateTime.now()
) {

    fun resolve() {
        status = FeedbackStatus.RESOLVED
    }

    fun isPending(): Boolean = status == FeedbackStatus.PENDING
    fun isResolved(): Boolean = status == FeedbackStatus.RESOLVED

}


enum class FeedbackStatus {
    PENDING,
    RESOLVED
}
