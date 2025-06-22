package my.assignment.chatbot.service

import my.assignment.chatbot.domain.ActivityType
import my.assignment.chatbot.domain.Role
import my.assignment.chatbot.domain.User
import my.assignment.chatbot.repository.UserRepository
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class UserService(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder,
    private val analyticsService: AnalyticsService
) {

    fun registerUser(signupRequest: SignupRequest): User {
        // 사용자명 중복 확인
        if (userRepository.findByName(signupRequest.username) != null) {
            throw IllegalArgumentException("이미 존재하는 사용자명입니다: ${signupRequest.username}")
        }

        // 이메일 중복 확인
        if (userRepository.findByEmail(signupRequest.email) != null) {
            throw IllegalArgumentException("이미 존재하는 이메일입니다: ${signupRequest.email}")
        }

        // 비밀번호 암호화
        val encodedPassword = passwordEncoder.encode(signupRequest.password)

        // 새 사용자 생성
        val newUser = User(
            name = signupRequest.username,
            password = encodedPassword,
            email = signupRequest.email,
            role = Role.USER
        )

        val saveUser=  userRepository.save(newUser)

        // 회원가입 활동 로그 기록
        analyticsService.logActivity(saveUser.username, ActivityType.SIGNUP, "사용자 회원가입")

        return saveUser
    }

    fun findByUsername(username: String): User? {
        return userRepository.findByName(username)
    }

    fun existsByUsername(username: String): Boolean {
        return userRepository.findByName(username) != null
    }

    fun existsByEmail(email: String): Boolean {
        return userRepository.findByEmail(email) != null
    }
}

data class SignupRequest(
    val username: String,
    val password: String,
    val email: String
)

data class SignupResponse(
    val id: Long,
    val username: String,
    val email: String,
    val message: String
)
