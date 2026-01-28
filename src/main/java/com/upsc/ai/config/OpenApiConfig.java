package com.upsc.ai.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.oas.models.tags.Tag;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;
import java.util.List;

@Configuration
public class OpenApiConfig {

    @Value("${spring.application.name:UPSC AI Backend}")
    private String applicationName;

    @Value("${info.app.version:0.0.1-SNAPSHOT}")
    private String version;

    @Value("${server.port:8080}")
    private String serverPort;

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(apiInfo())
                .servers(serverList())
                .components(securityComponents())
                .security(securityRequirements())
                .tags(apiTags());
    }

    private Info apiInfo() {
        return new Info()
                .title("UPSC AI Platform API")
                .description(
                        "AI-powered UPSC Test Platform - REST API for managing users, questions, tests, and PDF documents")
                .version(version)
                .contact(new Contact()
                        .name("UPSC AI Team")
                        .email("support@upsc-ai.com")
                        .url("https://upsc-ai.com"))
                .license(new License()
                        .name("Apache 2.0")
                        .url("https://www.apache.org/licenses/LICENSE-2.0.html"));
    }

    private List<Server> serverList() {
        return Arrays.asList(
                new Server()
                        .url("http://localhost:" + serverPort)
                        .description("Local Development Server"),
                new Server()
                        .url("https://api.upsc-ai.com")
                        .description("Production Server"),
                new Server()
                        .url("https://staging-api.upsc-ai.com")
                        .description("Staging Server"));
    }

    private Components securityComponents() {
        return new Components()
                .addSecuritySchemes("bearerAuth",
                        new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("JWT authentication token. Obtain by logging in via /api/auth/login"));
    }

    private List<SecurityRequirement> securityRequirements() {
        return Arrays.asList(
                new SecurityRequirement().addList("bearerAuth"));
    }

    private List<Tag> apiTags() {
        return Arrays.asList(
                new Tag()
                        .name("Authentication")
                        .description("User authentication and authorization endpoints"),
                new Tag()
                        .name("Questions")
                        .description("Question management and PDF parsing endpoints"),
                new Tag()
                        .name("Tests")
                        .description("Test generation, execution, and submission endpoints"),
                new Tag()
                        .name("PDF Documents")
                        .description("PDF upload, management, and download endpoints"));
    }
}
