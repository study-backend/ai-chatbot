package my.assignment.chatbot.controller

import jakarta.validation.Valid
import my.assignment.chatbot.controller.dto.ChatRequestDto
import my.assignment.chatbot.controller.dto.ChatResponseDto
import my.assignment.chatbot.controller.dto.ThreadWithChatsDto
import my.assignment.chatbot.domain.Threads
import my.assignment.chatbot.domain.User
import my.assignment.chatbot.service.ChatService
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter
import java.util.concurrent.CompletableFuture

@RestController
@RequestMapping("/api/chat")
class ChatController(
    private val chatService: ChatService
) {

    @PostMapping
    fun createChat(
        @Valid @RequestBody request: ChatRequestDto,
        @AuthenticationPrincipal user: User
    ): Any {
        return if (request.isStreaming) {
            createStreamingChat(request, user)
        } else {
            createNormalChat(request, user)
        }
    }

    private fun createStreamingChat(
        request: ChatRequestDto,
        user: User
    ): SseEmitter {
        val emitter = SseEmitter(30_000L)

        CompletableFuture.runAsync {
            try {
                chatService.createChatWithStreamingResponse(
                    user = user,
                    question = request.question,
                    model = request.model,
                    emitter = emitter
                )
            } catch (e: Exception) {
                emitter.completeWithError(e)
            }
        }

        return emitter
    }

    private fun createNormalChat(
        request: ChatRequestDto,
        user: User
    ): ResponseEntity<ChatResponseDto> {
        val chat = chatService.createChat(
            user = user,
            question = request.question,
            isStreaming = false,
            model = request.model
        )

        val response = ChatResponseDto(
            id = chat.id,
            question = chat.question,
            answer = chat.answer,
            threadId = chat.threads.id,
            createdAt = chat.createdAt
        )

        return ResponseEntity.ok(response)
    }


    /**
     * 대화 생성
     */
    @PostMapping("/stream")
    fun createChatStream(
        @Valid @RequestBody request: ChatRequestDto,
        @AuthenticationPrincipal user: User
    ): SseEmitter {
        if (request.isStreaming) {
            val emitter = SseEmitter(30_000L) // 30초 타임아웃 설정

            CompletableFuture.runAsync {
                try {
                    // 스트리밍 응답 처리 (실제로는 WebSocket이나 Server-Sent Events 사용)
                    chatService.createChatWithStreamingResponse(
                        user = user,
                        question = request.question,
                        model = request.model,
                        emitter = emitter
                    )
                } catch (e: Exception) {
                    emitter.send(
                        SseEmitter.event()
                            .name("error")
                            .data(mapOf("error" to e.message))
                    )
                    emitter.completeWithError(e)
                }
            }
            return emitter
        } else
            throw IllegalArgumentException("Streaming 요청이 아닙니다. isStreaming=true로 설정해주세요.")
    }

    /**
     * 대화 목록 조회 (스레드 단위로 그룹화)
     */
    @GetMapping("/threads")
    fun getUserThreads(
        @AuthenticationPrincipal user: User,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "10") size: Int,
        @RequestParam(defaultValue = "desc") sortDirection: String
    ): ResponseEntity<Page<ThreadWithChatsDto>> {
        val sort = if (sortDirection.lowercase() == "asc") {
            Sort.by("createdAt").ascending()
        } else {
            Sort.by("createdAt").descending()
        }

        val pageable = PageRequest.of(page, size, sort)
        val threads = chatService.getUserThreadsWithChats(user, pageable)

        return ResponseEntity.ok(threads)
    }

    /**
     * 특정 스레드의 채팅 목록 조회
     */
    @GetMapping("/threads/{threadId}/chats")
    fun getThreadChats(
        @PathVariable threadId: Long,
        @AuthenticationPrincipal user: User,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int,
        @RequestParam(defaultValue = "asc") sortDirection: String
    ): ResponseEntity<Page<ChatResponseDto>> {
        val sort = if (sortDirection.lowercase() == "asc") {
            Sort.by("createdAt").ascending()
        } else {
            Sort.by("createdAt").descending()
        }

        val pageable = PageRequest.of(page, size, sort)
        val chats = chatService.getThreadChats(threadId, user, pageable)

        val chatDtos = chats.map { chat ->
            ChatResponseDto(
                id = chat.id,
                question = chat.question,
                answer = chat.answer,
                threadId = chat.threads.id,
                createdAt = chat.createdAt
            )
        }

        return ResponseEntity.ok(chatDtos)
    }

    /**
     * 스레드 삭제
     */
    @DeleteMapping("/threads/{threadId}")
    fun deleteThread(
        @PathVariable threadId: Long,
        @AuthenticationPrincipal user: User
    ): ResponseEntity<Map<String, String>> {
        return try {
            chatService.deleteThread(threadId, user)
            ResponseEntity.ok(mapOf("message" to "스레드가 성공적으로 삭제되었습니다."))
        } catch (e: IllegalArgumentException) {
            ResponseEntity.badRequest().body(mapOf("error" to e.message!!))
        } catch (e: IllegalAccessException) {
            ResponseEntity.status(403).body(mapOf("error" to e.message!!))
        }
    }
}
