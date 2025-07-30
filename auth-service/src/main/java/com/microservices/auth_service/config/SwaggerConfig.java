package com.microservices.auth_service.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Auth Service API")
                        .version("1.0")
                        .description("인증 API 문서")
                        .contact(new Contact()
                                .name("Microservices Team")
                                .email("team@example.com")))
                .servers(List.of(
                        new Server().url("http://localhost:8081").description("Local Server"),
                        new Server().url("https://auth-service.example.com").description("Production Server")
                ));
    }
}
