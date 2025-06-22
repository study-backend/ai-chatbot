package my.assignment.chatbot.controller

import jakarta.validation.Valid
import my.assignment.chatbot.config.security.util.JwtTokenUtil
import my.assignment.chatbot.controller.dto.SignupRequestDto
import my.assignment.chatbot.domain.ActivityType
import my.assignment.chatbot.service.AnalyticsService
import my.assignment.chatbot.service.SignupRequest
import my.assignment.chatbot.service.SignupResponse
import my.assignment.chatbot.service.UserService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/auth")
class AuthController(
    private val authenticationManager: AuthenticationManager,
    private val jwtTokenUtil: JwtTokenUtil,
    private val userDetailsService: UserDetailsService,
    private val userService: UserService,
    private val analyticsService: AnalyticsService

) {

    @PostMapping("/signup")
    fun signup(@Valid @RequestBody signupRequest: SignupRequestDto): ResponseEntity<SignupResponse> {
        return try {
            val user = userService.registerUser(
                SignupRequest(
                    username = signupRequest.username,
                    password = signupRequest.password,
                    email = signupRequest.email
                )
            )

            val response = SignupResponse(
                id = user.id,
                username = user.username,
                email = user.email,
                message = "회원가입이 성공적으로 완료되었습니다."
            )

            ResponseEntity.status(HttpStatus.CREATED).body(response)
        } catch (e: IllegalArgumentException) {
            ResponseEntity.badRequest().body(
                SignupResponse(
                    id = 0,
                    username = "",
                    email = "",
                    message = e.message ?: "회원가입 중 오류가 발생했습니다."
                )
            )
        }
    }


    @PostMapping("/login")
    fun login(@RequestBody loginRequest: LoginRequest): ResponseEntity<TokenResponse> {
        val authentication = authenticationManager.authenticate(
            UsernamePasswordAuthenticationToken(
                loginRequest.username,
                loginRequest.password
            )
        )

        val userDetails = userDetailsService.loadUserByUsername(loginRequest.username)
        val token = jwtTokenUtil.generateToken(userDetails)

        // 로그인 성공 시 활동 로그 기록
        analyticsService.logActivity(userDetails.username, ActivityType.LOGIN, "사용자 로그인")



        return ResponseEntity.ok(TokenResponse(token))
    }

    @GetMapping("/check-username/{username}")
    fun checkUsername(@PathVariable username: String): ResponseEntity<Map<String, Boolean>> {
        val exists = userService.existsByUsername(username)
        return ResponseEntity.ok(mapOf("exists" to exists))
    }

    @GetMapping("/check-email/{email}")
    fun checkEmail(@PathVariable email: String): ResponseEntity<Map<String, Boolean>> {
        val exists = userService.existsByEmail(email)
        return ResponseEntity.ok(mapOf("exists" to exists))
    }

}

data class LoginRequest(
    val username: String,
    val password: String
)

data class TokenResponse(
    val token: String
)
