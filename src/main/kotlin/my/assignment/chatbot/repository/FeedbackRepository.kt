package my.assignment.chatbot.repository

import my.assignment.chatbot.domain.Chat
import my.assignment.chatbot.domain.Feedback
import my.assignment.chatbot.domain.FeedbackStatus
import my.assignment.chatbot.domain.User
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

interface FeedbackRepository : JpaRepository<Feedback, Long> {

    fun findByUser(user: User, pageable: Pageable): Page<Feedback>

    fun findByChat(chat: Chat): List<Feedback>

    fun findByStatus(status: FeedbackStatus, pageable: Pageable): Page<Feedback>

    @Query("SELECT f FROM Feedback f WHERE f.user = :user AND f.status = :status")
    fun findByUserAndStatus(user: User, status: FeedbackStatus, pageable: Pageable): Page<Feedback>

    @Query("SELECT f FROM Feedback f WHERE f.user = :user AND f.isPositive = :isPositive")
    fun findByUserAndIsPositive(user: User, isPositive: Boolean, pageable: Pageable): Page<Feedback>

    @Query("SELECT f FROM Feedback f WHERE f.isPositive = :isPositive")
    fun findByIsPositive(isPositive: Boolean, pageable: Pageable): Page<Feedback>

    @Query("SELECT COUNT(f) FROM Feedback f WHERE f.isPositive = true")
    fun countPositiveFeedbacks(): Long

    @Query("SELECT COUNT(f) FROM Feedback f WHERE f.isPositive = false")
    fun countNegativeFeedbacks(): Long

    fun existsByChatAndUser(chat: Chat, user: User): Boolean
}
