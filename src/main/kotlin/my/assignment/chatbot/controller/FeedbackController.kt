package my.assignment.chatbot.controller

import jakarta.validation.Valid
import my.assignment.chatbot.controller.dto.FeedbackRequestDto
import my.assignment.chatbot.controller.dto.FeedbackResponseDto
import my.assignment.chatbot.controller.dto.FeedbackStatusUpdateDto
import my.assignment.chatbot.domain.User
import my.assignment.chatbot.service.FeedbackService
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/feedback")
class FeedbackController(
    private val feedbackService: FeedbackService
) {

    /**
     * 피드백 생성
     * - 사용자: 자신이 생성한 대화에만 피드백 생성 가능
     * - 관리자: 모든 대화에 피드백 생성 가능
     * - 각 사용자는 하나의 대화에 하나의 피드백만 생성 가능
     */
    @PostMapping
    fun createFeedback(
        @Valid @RequestBody request: FeedbackRequestDto,
        @AuthenticationPrincipal user: User
    ): ResponseEntity<Any> {
        return try {
            val feedback = feedbackService.createFeedback(request, user)
            ResponseEntity.ok(feedback)
        } catch (e: IllegalArgumentException) {
            ResponseEntity.badRequest().body(mapOf("error" to e.message))
        } catch (e: IllegalAccessException) {
            ResponseEntity.status(403).body(mapOf("error" to e.message))
        }
    }

    /**
     * 피드백 목록 조회
     * - 사용자: 자신이 생성한 피드백만 조회
     * - 관리자: 모든 피드백 조회
     * - 생성일시 기준 정렬 및 페이지네이션 지원
     * - 긍정/부정 필터링 지원
     */
    @GetMapping
    fun getFeedbacks(
        @AuthenticationPrincipal user: User,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "10") size: Int,
        @RequestParam(defaultValue = "desc") sortDirection: String,
        @RequestParam(required = false) isPositive: Boolean?
    ): ResponseEntity<Page<FeedbackResponseDto>> {
        val sort = if (sortDirection.lowercase() == "asc") {
            Sort.by("createdAt").ascending()
        } else {
            Sort.by("createdAt").descending()
        }

        val pageable = PageRequest.of(page, size, sort)
        val feedbacks = feedbackService.getFeedbacks(user, pageable, isPositive)

        return ResponseEntity.ok(feedbacks)
    }

    /**
     * 피드백 상태 변경 (관리자 전용)
     */
    @PutMapping("/{feedbackId}/status")
    fun updateFeedbackStatus(
        @PathVariable feedbackId: Long,
        @Valid @RequestBody statusUpdate: FeedbackStatusUpdateDto,
        @AuthenticationPrincipal user: User
    ): ResponseEntity<Any> {
        return try {
            val feedback = feedbackService.updateFeedbackStatus(feedbackId, statusUpdate, user)
            ResponseEntity.ok(feedback)
        } catch (e: IllegalArgumentException) {
            ResponseEntity.badRequest().body(mapOf("error" to e.message))
        } catch (e: IllegalAccessException) {
            ResponseEntity.status(403).body(mapOf("error" to e.message))
        }
    }

    /**
     * 피드백 통계 조회 (관리자 전용)
     */
    @GetMapping("/stats")
    fun getFeedbackStats(
        @AuthenticationPrincipal user: User
    ): ResponseEntity<Any> {
        return try {
            val stats = feedbackService.getFeedbackStats(user)
            ResponseEntity.ok(stats)
        } catch (e: IllegalAccessException) {
            ResponseEntity.status(403).body(mapOf("error" to e.message))
        }
    }
}
