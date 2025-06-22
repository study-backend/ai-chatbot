package my.assignment.chatbot.domain

import jakarta.persistence.Column
import jakarta.persistence.Entity
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
@Table(name = "chats")
class Chat(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(nullable = false, columnDefinition = "TEXT")
    val question: String,

    @Column(nullable = false, columnDefinition = "TEXT")
    var answer: String,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "threads_id", nullable = false)
    val threads: Threads,

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    val createdAt: OffsetDateTime = OffsetDateTime.now()
)
