package com.example.autobank.config

import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Info
import io.swagger.v3.oas.models.info.Contact
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class OpenAPIConfig {
    
    @Bean
    fun customOpenAPI(): OpenAPI {
        return OpenAPI()
            .info(
                Info()
                    .title("Autobank API")
                    .version("1.0.0")
                    .description("API documentation for Autobank application")
                    .contact(
                        Contact()
                            .name("Autobank Team")
                            .email("support@autobank.example.com")
                    )
            )
    }
}
