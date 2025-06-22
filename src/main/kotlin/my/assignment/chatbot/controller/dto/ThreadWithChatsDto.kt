package my.assignment.chatbot.controller.dto

import java.time.OffsetDateTime

data class ThreadWithChatsDto(
    val id: Long,
    val userName: String,
    val userEmail: String,
    val createdAt: OffsetDateTime,
    val lastActivityAt: OffsetDateTime,
    val chatCount: Long
)

data class ChatRequestDto(
    val question: String,
    val isStreaming: Boolean = false,
    val model: String? = null
)

data class ChatResponseDto(
    val id: Long,
    val question: String,
    val answer: String,
    val threadId: Long,
    val createdAt: OffsetDateTime
)

data class ThreadResponseDto(
    val id: Long,
    val userName: String,
    val userEmail: String,
    val createdAt: OffsetDateTime,
    val lastActivityAt: OffsetDateTime,
    val chatCount: Long,
    val chats: List<ChatResponseDto>? = null
)

