package my.assignment.chatbot.service

import my.assignment.chatbot.domain.Chat
import org.springframework.stereotype.Service
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Executors

@Service
class OpenAiService {
    private val executor = Executors.newCachedThreadPool()

    /**
     * OpenAI API를 호출하여 응답을 생성합니다.
     * 실제 구현에서는 OpenAI API 클라이언트를 사용해야 합니다.
     */
    fun generateResponse(
        question: String,
        chatHistory: List<Chat>,
        isStreaming: Boolean,
        model: String?
    ): String {
        // TODO: 실제 OpenAI API 호출 구현
        // 현재는 더미 응답 반환
        val modelToUse = model ?: "gpt-3.5-turbo"

        // 채팅 히스토리를 OpenAI 형식으로 변환
        val messages = buildList {
            chatHistory.forEach { chat ->
                add(mapOf("role" to "user", "content" to chat.question))
                add(mapOf("role" to "assistant", "content" to chat.answer))
            }
            add(mapOf("role" to "user", "content" to question))
        }

        return "[$modelToUse 로 생성된 응답] $question 에 대한 답변입니다. (채팅 히스토리 ${chatHistory.size}개 참고)"
    }

    /**
     * 스트리밍 응답을 위한 메서드
     */
    fun generateStreamingResponse(
        question: String,
        chatHistory: List<Chat>,
        model: String?,
        onChunk: (String) -> Unit
    ) {
        // TODO: 실제 스트리밍 API 호출 구현
        val modelToUse = model ?: "gpt-3.5-turbo"

        // TODO: 실제 OpenAI API 스트리밍 호출 구현
        // 현재는 시뮬레이션
        val response = "[$modelToUse 스트리밍] $question 에 대한 실시간 응답입니다. 이것은 긴 응답의 예시입니다."

        // 단어별로 나누어 스트리밍 시뮬레이션
        response.split(" ").forEach { word ->
            onChunk("$word ")
            try {
                Thread.sleep(100) // 100ms 딜레이
            } catch (e: InterruptedException) {
                Thread.currentThread().interrupt()
                return
            }
        }
    }

    /**
     * 비동기 스트리밍 응답 생성
     */
    fun generateStreamingResponseAsync(
        question: String,
        chatHistory: List<Chat>,
        model: String?,
        onChunk: (String) -> Unit,
        onComplete: () -> Unit,
        onError: (Exception) -> Unit
    ) {
        CompletableFuture.runAsync({
            try {
                generateStreamingResponse(question, chatHistory, model, onChunk)
                onComplete()
            } catch (e: Exception) {
                onError(e)
            }
        }, executor)
    }

}
