package my.assignment.chatbot.service

import my.assignment.chatbot.controller.dto.ThreadWithChatsDto
import my.assignment.chatbot.domain.ActivityType
import my.assignment.chatbot.domain.Chat
import my.assignment.chatbot.domain.Role
import my.assignment.chatbot.domain.Threads
import my.assignment.chatbot.domain.User
import my.assignment.chatbot.repository.ChatRepository
import my.assignment.chatbot.repository.ThreadsRepository
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter
import java.time.OffsetDateTime

@Service
@Transactional
class ChatService(
    private val chatRepository: ChatRepository,
    private val threadsRepository: ThreadsRepository,
    private val openAiService: OpenAiService,
    private val analyticsService: AnalyticsService
) {

    fun createChat(
        user: User,
        question: String,
        isStreaming: Boolean = false,
        model: String? = null
    ): Chat {
        // 활성 스레드 가져오기 또는 새 스레드 생성
        val threads = getOrCreateActiveThread(user)

        // 이전 대화 히스토리 가져오기
        val chatHistory = getChatHistory(threads)

        // OpenAI API 호출하여 답변 생성
        val answer = openAiService.generateResponse(
            question = question,
            chatHistory = chatHistory,
            isStreaming = isStreaming,
            model = model
        )

        // 채팅 생성
        val chat = Chat(
            question = question,
            answer = answer,
            threads = threads
        )

        // 스레드 활동 시간 업데이트
        threads.updateLastActivity()
        threadsRepository.save(threads)

        val savedChat = chatRepository.save(chat)

        // 채팅 생성 활동 로그 기록
        analyticsService.logActivity(user.username, ActivityType.CHAT_CREATED, "대화 생성: ${question.take(50)}")

        return savedChat
    }

    fun createChatWithStreamingResponse(
        user: User,
        question: String,
        model: String? = null,
        emitter: SseEmitter
    ): Chat {
        val thread = getOrCreateActiveThread(user)
        val chatHistory = getChatHistory(thread)

        // 빈 채팅 엔티티 생성 (나중에 업데이트)
        val chat = Chat(
            question = question,
            answer = "", // 초기에는 빈 문자열
            threads = thread
        )
        val savedChat = chatRepository.save(chat)


        emitter.send(
            SseEmitter.event()
                .name("chat-start")
                .data(mapOf(
                    "chatId" to savedChat.id,
                    "threadId" to thread.id,
                    "question" to question
                ))
        )

        // OpenAI API를 통한 스트리밍 응답 생성
        val fullAnswer = StringBuilder()
        openAiService.generateStreamingResponseAsync(
            question = question,
            chatHistory = chatHistory,
            model = model,
            onChunk = { chunk ->
                try {
                    // 각 청크를 클라이언트에 전송
                    fullAnswer.append(chunk)
                    emitter.send(
                        SseEmitter.event()
                            .name("chunk")
                            .data(mapOf("content" to chunk))
                    )
                } catch (e: Exception) {
                    emitter.completeWithError(e)
                }
            },
            onComplete = {
                try {
                    // 최종 답변으로 채팅 업데이트
                    savedChat.answer = fullAnswer.toString()
                    chatRepository.save(savedChat)

                    // 스레드 활동 시간 업데이트
                    thread.updateLastActivity()
                    threadsRepository.save(thread)

                    // 완료 이벤트
                    emitter.send(
                        SseEmitter.event()
                            .name("complete")
                            .data(mapOf(
                                "chatId" to savedChat.id,
                                "fullAnswer" to fullAnswer.toString()
                            ))
                    )

                    // 활동 로그 기록
                    analyticsService.logActivity(user.username, ActivityType.CHAT_CREATED, "스트리밍 대화 생성: ${question.take(50)}")

                    emitter.complete()
                } catch (e: Exception) {
                    emitter.completeWithError(e)
                }
            },
            onError = { e ->
                emitter.send(
                    SseEmitter.event()
                        .name("error")
                        .data(mapOf("error" to e.message))
                )
                emitter.completeWithError(e)
            }
        )
        return savedChat
    }


    private fun getOrCreateActiveThread(user: User): Threads {
        val cutoffTime = OffsetDateTime.now().minusMinutes(30)
        val activeThread = threadsRepository.findActiveThreadsByUser(user, cutoffTime)

        return if (activeThread != null) {
            activeThread
        } else {
            val newThread = Threads(user = user)
            threadsRepository.save(newThread)
        }
    }

    @Transactional(readOnly = true)
    fun getChatHistory(threads: Threads): List<Chat> {
        return chatRepository.findByThreadsOrderByCreatedAtAsc(threads)
    }

    @Transactional(readOnly = true)
    fun getUserThreadsWithChats(user: User, pageable: Pageable): Page<ThreadWithChatsDto> {
        return if (user.role == Role.ADMIN) {
            // 관리자는 모든 스레드 조회 가능
            threadsRepository.findAllThreadsWithChats(pageable)
        } else {
            // 일반 사용자는 자신의 스레드만 조회 가능
            threadsRepository.findByUserWithChats(user, pageable)
        }
    }

    @Transactional(readOnly = true)
    fun getThreadChats(threadId: Long, user: User, pageable: Pageable): Page<Chat> {
        val threads = threadsRepository.findById(threadId)
            .orElseThrow { IllegalArgumentException("스레드를 찾을 수 없습니다: $threadId") }

        // 권한 확인
        if (user.role != Role.ADMIN && threads.user.id != user.id) {
            throw IllegalAccessException("해당 스레드에 접근할 권한이 없습니다.")
        }

        return chatRepository.findByThreadsWithPagination(threads, pageable)
    }

    fun deleteThread(threadId: Long, user: User) {
        val thread = threadsRepository.findById(threadId)
            .orElseThrow { IllegalArgumentException("스레드를 찾을 수 없습니다: $threadId") }

        // 권한 확인 - 본인의 스레드만 삭제 가능
        if (thread.user.id != user.id) {
            throw IllegalAccessException("해당 스레드를 삭제할 권한이 없습니다.")
        }

        threadsRepository.delete(thread)
    }


    @Transactional(readOnly = true)
    fun getUserThreads(user: User): List<Threads> {
        return threadsRepository.findByUserOrderByCreatedAtDesc(user)
    }
}
