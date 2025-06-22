package my.assignment.chatbot.repository

import my.assignment.chatbot.domain.ActivityType
import my.assignment.chatbot.domain.UserActivityLog
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.time.OffsetDateTime

interface UserActivityLogRepository : JpaRepository<UserActivityLog, Long> {

    @Query("""
        SELECT COUNT(u) FROM UserActivityLog u 
        WHERE u.activityType = :activityType 
        AND u.createdAt >= :startDate 
        AND u.createdAt < :endDate
    """)
    fun countByActivityTypeAndDateRange(
        @Param("activityType") activityType: ActivityType,
        @Param("startDate") startDate: OffsetDateTime,
        @Param("endDate") endDate: OffsetDateTime
    ): Long

    @Query("""
        SELECT u FROM UserActivityLog u 
        WHERE u.createdAt >= :startDate 
        AND u.createdAt < :endDate
        ORDER BY u.createdAt DESC
    """)
    fun findByDateRange(
        @Param("startDate") startDate: OffsetDateTime,
        @Param("endDate") endDate: OffsetDateTime
    ): List<UserActivityLog>
}
