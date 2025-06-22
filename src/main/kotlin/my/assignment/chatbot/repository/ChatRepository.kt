package my.assignment.chatbot.repository

import my.assignment.chatbot.domain.Chat
import my.assignment.chatbot.domain.Threads
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.time.OffsetDateTime

interface ChatRepository : JpaRepository<Chat, Long> {

    fun findByThreadsOrderByCreatedAtAsc(threads: Threads): List<Chat>

    fun findByThreadsOrderByCreatedAtAsc(threads: Threads, pageable: Pageable): Page<Chat>


    fun findByThreadsOrderByCreatedAtDesc(threads: Threads): List<Chat>

    @Query("SELECT c FROM Chat c WHERE c.threads = :thread")
    fun findByThreadsWithPagination(thread: Threads, pageable: Pageable): Page<Chat>

    @Query("""
        SELECT c FROM Chat c 
        WHERE c.createdAt >= :startDate 
        AND c.createdAt < :endDate
        ORDER BY c.createdAt DESC
    """)
    fun findByDateRange(
        @Param("startDate") startDate: OffsetDateTime,
        @Param("endDate") endDate: OffsetDateTime
    ): List<Chat>

}
