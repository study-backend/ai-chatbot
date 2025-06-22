package my.assignment.chatbot.controller.dto

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class SignupRequestDto(
    @field:NotBlank(message = "사용자명은 필수입니다")
    @field:Size(min = 3, max = 20, message = "사용자명은 3-20자 사이여야 합니다")
    val username: String,

    @field:NotBlank(message = "비밀번호는 필수입니다")
    @field:Size(min = 8, max = 100, message = "비밀번호는 8자 이상이어야 합니다")
    val password: String,

    @field:NotBlank(message = "이메일은 필수입니다")
    @field:Email(message = "올바른 이메일 형식이 아닙니다")
    val email: String
)

