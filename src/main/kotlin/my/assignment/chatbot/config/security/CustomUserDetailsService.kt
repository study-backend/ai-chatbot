package my.assignment.chatbot.config.security

import my.assignment.chatbot.repository.UserRepository
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.stereotype.Service

@Service
class CustomUserDetailsService(
    private val userRepository: UserRepository
) : UserDetailsService {

    override fun loadUserByUsername(username: String): UserDetails {
        return userRepository.findByEmail(username)
            ?: throw UsernameNotFoundException("사용자를 찾을 수 없습니다: $username")
    }
}
