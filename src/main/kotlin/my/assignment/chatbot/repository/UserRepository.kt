package my.assignment.chatbot.repository

import my.assignment.chatbot.domain.User
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.time.OffsetDateTime

interface UserRepository : JpaRepository<User, Long> {
    fun findByName(name: String): User?
    fun findByEmail(email: String): User?

    @Query("""
        SELECT COUNT(u) FROM User u 
        WHERE u.createdAt >= :startDate 
        AND u.createdAt < :endDate
    """)
    fun countByDateRange(
        @Param("startDate") startDate: OffsetDateTime,
        @Param("endDate") endDate: OffsetDateTime
    ): Long

}
