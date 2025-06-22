package my.assignment.chatbot.controller

import my.assignment.chatbot.domain.User
import my.assignment.chatbot.service.AnalyticsService
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/analytics")
class AnalyticsController(
    private val analyticsService: AnalyticsService
) {
    /**
     * 사용자 활동 기록 요청 (관리자 전용)
     * 요청 시점으로부터 하루 동안의 회원가입, 로그인, 대화 생성 수
     */
    @GetMapping("/daily-activity")
    fun getDailyUserActivity(
        @AuthenticationPrincipal user: User
    ): ResponseEntity<Any> {
        return try {
            val activity = analyticsService.getDailyUserActivity(user)
            ResponseEntity.ok(activity)
        } catch (e: IllegalAccessException) {
            ResponseEntity.status(403).body(mapOf("error" to e.message))
        }
    }

    /**
     * 보고서 생성 요청 (관리자 전용)
     * 요청 시점으로부터 하루 동안의 모든 사용자 대화 목록을 CSV로 생성
     */
    @GetMapping("/daily-report")
    fun generateDailyChatReport(
        @AuthenticationPrincipal user: User
    ): ResponseEntity<*> {
        return try {
            analyticsService.generateDailyChatReport(user)
        } catch (e: IllegalAccessException) {
            ResponseEntity.status(403).body(mapOf("error" to e.message))
        }
    }
}