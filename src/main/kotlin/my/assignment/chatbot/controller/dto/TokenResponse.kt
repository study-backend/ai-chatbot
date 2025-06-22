package my.assignment.chatbot.controller.dto

data class TokenResponse(
    val token: String,
    val type: String = "Bearer"
)

