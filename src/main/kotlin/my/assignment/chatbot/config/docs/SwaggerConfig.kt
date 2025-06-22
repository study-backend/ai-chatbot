package my.assignment.chatbot.config.docs

import io.swagger.v3.oas.models.Components
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Info
import io.swagger.v3.oas.models.security.SecurityRequirement
import io.swagger.v3.oas.models.security.SecurityScheme
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration


@Configuration
class SwaggerConfig {

    @Bean
    fun openAPI(): OpenAPI {
        return OpenAPI()
            .info(
                Info()
                    .title("Chatbot API")
                    .description("Spring Boot JWT 인증 기반 Chatbot API 문서")
                    .version("1.0.0")
            )
            .components(
                Components()
                    .addSecuritySchemes("bearerAuth",
                        SecurityScheme()
                            .type(SecurityScheme.Type.HTTP)
                            .scheme("bearer")
                            .bearerFormat("JWT")
                            .description("JWT Bearer Token을 입력하세요")
                    )
            )
            .addSecurityItem(SecurityRequirement().addList("bearerAuth"))
    }
}
