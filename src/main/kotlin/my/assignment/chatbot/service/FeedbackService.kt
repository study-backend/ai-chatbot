package my.assignment.chatbot.service

import my.assignment.chatbot.controller.dto.FeedbackRequestDto
import my.assignment.chatbot.controller.dto.FeedbackResponseDto
import my.assignment.chatbot.controller.dto.FeedbackStatusUpdateDto
import my.assignment.chatbot.domain.Feedback
import my.assignment.chatbot.domain.Role
import my.assignment.chatbot.domain.User
import my.assignment.chatbot.repository.ChatRepository
import my.assignment.chatbot.repository.FeedbackRepository
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional


@Service
class FeedbackService(
    private val feedbackRepository: FeedbackRepository,
    private val chatRepository: ChatRepository
) {

    @Transactional
    fun createFeedback(request: FeedbackRequestDto, user: User): FeedbackResponseDto {
        val chat = chatRepository.findById(request.chatId)
            .orElseThrow { IllegalArgumentException("채팅을 찾을 수 없습니다.") }

        // 권한 확인: 사용자는 자신의 채팅만, 관리자는 모든 채팅에 피드백 가능
        if (user.role != Role.ADMIN && chat.threads.user.id != user.id) {
            throw IllegalAccessException("해당 채팅에 대한 피드백 권한이 없습니다.")
        }

        // 중복 피드백 확인 (사용자당 하나의 대화에 하나의 피드백만)
        if (feedbackRepository.existsByChatAndUser(chat, user)) {
            throw IllegalArgumentException("이미 해당 채팅에 대한 피드백이 존재합니다.")
        }

        val feedback = Feedback(
            user = user,
            chat = chat,
            isPositive = request.isPositive
        )

        val savedFeedback = feedbackRepository.save(feedback)

        return FeedbackResponseDto(
            id = savedFeedback.id,
            userId = savedFeedback.user.email,
            userName = savedFeedback.user.name,
            chatId = savedFeedback.chat.id,
            isPositive = savedFeedback.isPositive,
            status = savedFeedback.status,
            createdAt = savedFeedback.createdAt
        )
    }

    @Transactional(readOnly = true)
    fun getFeedbacks(
        user: User,
        pageable: Pageable,
        isPositive: Boolean? = null
    ): Page<FeedbackResponseDto> {
        val feedbacks = if (user.role == Role.ADMIN) {
            // 관리자는 모든 피드백 조회 가능
            when (isPositive) {
                null -> feedbackRepository.findAll(pageable)
                else -> feedbackRepository.findByIsPositive(isPositive, pageable)
            }
        } else {
            // 일반 사용자는 자신의 피드백만 조회 가능
            when (isPositive) {
                null -> feedbackRepository.findByUser(user, pageable)
                else -> feedbackRepository.findByUserAndIsPositive(user, isPositive, pageable)
            }
        }

        return feedbacks.map { feedback ->
            FeedbackResponseDto(
                id = feedback.id,
                userId = feedback.user.email,
                userName = feedback.user.name,
                chatId = feedback.chat.id,
                isPositive = feedback.isPositive,
                status = feedback.status,
                createdAt = feedback.createdAt
            )
        }
    }

    @Transactional
    fun updateFeedbackStatus(
        feedbackId: Long,
        statusUpdate: FeedbackStatusUpdateDto,
        user: User
    ): FeedbackResponseDto {
        // 관리자 권한 확인
        if (user.role != Role.ADMIN) {
            throw IllegalAccessException("피드백 상태 변경 권한이 없습니다.")
        }

        val feedback = feedbackRepository.findById(feedbackId)
            .orElseThrow { IllegalArgumentException("피드백을 찾을 수 없습니다.") }

        feedback.status = statusUpdate.status
        val savedFeedback = feedbackRepository.save(feedback)

        return FeedbackResponseDto(
            id = savedFeedback.id,
            userId = savedFeedback.user.email,
            userName = savedFeedback.user.name,
            chatId = savedFeedback.chat.id,
            isPositive = savedFeedback.isPositive,
            status = savedFeedback.status,
            createdAt = savedFeedback.createdAt
        )
    }

    @Transactional(readOnly = true)
    fun getFeedbackStats(user: User): Map<String, Long> {
        if (user.role != Role.ADMIN) {
            throw IllegalAccessException("통계 조회 권한이 없습니다.")
        }

        val positive = feedbackRepository.countPositiveFeedbacks()
        val negative = feedbackRepository.countNegativeFeedbacks()

        return mapOf(
            "positive" to positive,
            "negative" to negative,
            "total" to (positive + negative)
        )
    }
}
