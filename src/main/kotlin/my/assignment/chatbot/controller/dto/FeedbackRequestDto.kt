package my.assignment.chatbot.controller.dto

import my.assignment.chatbot.domain.FeedbackStatus
import java.time.OffsetDateTime


data class FeedbackRequestDto(
    val chatId: Long,
    val isPositive: Boolean
)

data class FeedbackResponseDto(
    val id: Long,
    val userId: String,
    val userName: String,
    val chatId: Long,
    val isPositive: Boolean,
    val status: FeedbackStatus,
    val createdAt: OffsetDateTime
)

data class FeedbackStatusUpdateDto(
    val status: FeedbackStatus
)

