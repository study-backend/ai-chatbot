package my.assignment.chatbot.config.security

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import my.assignment.chatbot.config.security.util.JwtTokenUtil
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter


@Component
class JwtAuthenticationFilter(
    private val jwtTokenUtil: JwtTokenUtil,
    private val userDetailsService: UserDetailsService
) : OncePerRequestFilter() {

    // JWT 필터를 적용하지 않을 경로들
    private val excludeUrls = listOf(
        "/api/auth/",
        "/h2-console/",
        "/v3/api-docs",
        "/swagger-ui",
        "/swagger-ui.html",
        "/swagger-resources/",
        "/webjars/"
    )

    override fun shouldNotFilter(request: HttpServletRequest): Boolean {
        val requestURI = request.requestURI
        return excludeUrls.any { requestURI.startsWith(it) }
    }


    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        val authHeader = request.getHeader("Authorization")

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response)
            return
        }

        try {
            val jwt = authHeader.substring(7)

            if (jwtTokenUtil.isValidToken(jwt)) {
                val username = jwtTokenUtil.getUsernameFromToken(jwt)

                if (SecurityContextHolder.getContext().authentication == null) {
                    val userDetails = userDetailsService.loadUserByUsername(username)

                    if (jwtTokenUtil.validateToken(jwt, userDetails)) {
                        val authToken = UsernamePasswordAuthenticationToken(
                            userDetails,
                            null,
                            userDetails.authorities
                        )
                        authToken.details = WebAuthenticationDetailsSource().buildDetails(request)
                        SecurityContextHolder.getContext().authentication = authToken
                    }
                }
            }
        } catch (e: Exception) {
            logger.error("JWT 토큰 처리 중 오류 발생: ${e.message}")
        }

        filterChain.doFilter(request, response)
    }
}
