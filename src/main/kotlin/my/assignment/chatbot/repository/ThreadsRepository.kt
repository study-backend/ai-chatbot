package my.assignment.chatbot.repository

import my.assignment.chatbot.controller.dto.ThreadWithChatsDto
import my.assignment.chatbot.domain.Threads
import my.assignment.chatbot.domain.User
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.EntityGraph
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.time.OffsetDateTime

interface ThreadsRepository : JpaRepository<Threads, Long> {

    @Query("SELECT t FROM Threads t WHERE t.user = :user AND t.lastActivityAt > :cutoffTime ORDER BY t.lastActivityAt DESC")
    fun findActiveThreadsByUser(@Param("user") user: User, @Param("cutoffTime") cutoffTime: OffsetDateTime): Threads?

    fun findByUserOrderByCreatedAtDesc(user: User): List<Threads>

    @EntityGraph(attributePaths = ["chats"])
    @Query("SELECT t FROM Threads t WHERE t.user = :user ORDER BY t.lastActivityAt DESC")
    fun findByUserWithChatsGraph(@Param("user") user: User, pageable: Pageable): Page<Threads>

    @Query("""
        SELECT new my.assignment.chatbot.controller.dto.ThreadWithChatsDto(
            t.id, t.user.name, t.user.email, t.createdAt, t.lastActivityAt,
            COALESCE(COUNT(c), 0)
        )
        FROM Threads t 
        LEFT JOIN t.chats c
        WHERE t.user = :user
        GROUP BY t.id, t.user.name, t.user.email, t.createdAt, t.lastActivityAt
        ORDER BY t.lastActivityAt DESC
    """)
    fun findByUserWithChats(@Param("user") user: User, pageable: Pageable): Page<ThreadWithChatsDto>


    @Query("""
        SELECT new my.assignment.chatbot.controller.dto.ThreadWithChatsDto(
            t.id, t.user.name, t.user.email, t.createdAt, t.lastActivityAt,
            COALESCE(COUNT(c), 0)
        )
        FROM Threads t 
        LEFT JOIN t.chats c
        GROUP BY t.id, t.user.name, t.user.email, t.createdAt, t.lastActivityAt
        ORDER BY t.lastActivityAt DESC
    """)
    fun findAllThreadsWithChats(pageable: Pageable): Page<ThreadWithChatsDto>

}

