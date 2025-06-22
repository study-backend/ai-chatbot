package my.assignment.chatbot.service

import my.assignment.chatbot.domain.ActivityType
import my.assignment.chatbot.domain.Role
import my.assignment.chatbot.domain.User
import my.assignment.chatbot.domain.UserActivityLog
import my.assignment.chatbot.repository.ChatRepository
import my.assignment.chatbot.repository.UserActivityLogRepository
import my.assignment.chatbot.repository.UserRepository
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.io.StringWriter
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter

@Service
class AnalyticsService(
    private val userActivityLogRepository: UserActivityLogRepository,
    private val userRepository: UserRepository,
    private val chatRepository: ChatRepository

) {

    /**
     * 활동 로그 기록
     */
    @Transactional
    fun logActivity(username: String, activityType: ActivityType, description: String? = null) {
        val user = userRepository.findByEmail(username) ?: throw IllegalArgumentException("사용자를 찾을 수 없습니다: $username")
        val log = UserActivityLog(
            user = user,
            activityType = activityType,
            description = description
        )
        userActivityLogRepository.save(log)
    }

    /**
     * 사용자 활동 기록 조회 (하루 동안)
     */
    @Transactional(readOnly = true)
    fun getDailyUserActivity(user: User): Map<String, Any> {
        if (user.role != Role.ADMIN) {
            throw IllegalAccessException("관리자만 사용자 활동 기록을 조회할 수 있습니다.")
        }

        val now = OffsetDateTime.now()
        val startOfDay = now.toLocalDate().atStartOfDay(now.offset).toOffsetDateTime()
        val endOfDay = startOfDay.plusDays(1)

        val signupCount = userActivityLogRepository.countByActivityTypeAndDateRange(
            ActivityType.SIGNUP, startOfDay, endOfDay
        )
        val loginCount = userActivityLogRepository.countByActivityTypeAndDateRange(
            ActivityType.LOGIN, startOfDay, endOfDay
        )
        val chatCount = userActivityLogRepository.countByActivityTypeAndDateRange(
            ActivityType.CHAT_CREATED, startOfDay, endOfDay
        )

        return mapOf(
            "date" to startOfDay.toLocalDate().toString(),
            "signupCount" to signupCount,
            "loginCount" to loginCount,
            "chatCreatedCount" to chatCount
        )
    }

    /**
     * CSV 보고서 생성 (하루 동안의 모든 대화)
     */
    @Transactional(readOnly = true)
    fun generateDailyChatReport(user: User): ResponseEntity<String> {
        if (user.role != Role.ADMIN) {
            throw IllegalAccessException("관리자만 보고서를 생성할 수 있습니다.")
        }

        val now = OffsetDateTime.now()
        val startOfDay = now.toLocalDate().atStartOfDay(now.offset).toOffsetDateTime()
        val endOfDay = startOfDay.plusDays(1)

        val chats = chatRepository.findByDateRange(startOfDay, endOfDay)

        val csvContent = generateCsvContent(chats)

        val headers = HttpHeaders().apply {
            contentType = MediaType.parseMediaType("text/csv")
            setContentDispositionFormData("attachment", "daily_chat_report_${startOfDay.toLocalDate()}.csv")
        }

        return ResponseEntity.ok()
            .headers(headers)
            .body(csvContent)
    }

    private fun generateCsvContent(chats: List<my.assignment.chatbot.domain.Chat>): String {
        val writer = StringWriter()
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")

        // CSV 헤더
        writer.appendLine("Chat ID,Thread ID,User Name,User Email,Question,Answer,Created At")

        // CSV 데이터
        chats.forEach { chat ->
            val line = listOf(
                chat.id.toString(),
                chat.threads.id.toString(),
                escapeCSV(chat.threads.user.name),
                escapeCSV(chat.threads.user.email),
                escapeCSV(chat.question),
                escapeCSV(chat.answer),
                chat.createdAt.format(formatter)
            ).joinToString(",")

            writer.appendLine(line)
        }

        return writer.toString()
    }

    private fun escapeCSV(value: String): String {
        return if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            "\"${value.replace("\"", "\"\"")}\""
        } else {
            value
        }
    }
}