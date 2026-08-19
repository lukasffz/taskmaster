package com.taskmaster.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI taskMasterOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("TaskMaster API")
                        .version("v1")
                        .description("API for project and task management with cookie-based JWT authentication."))
                .components(new Components()
                        .addSecuritySchemes("cookieAuth", new SecurityScheme()
                                .type(SecurityScheme.Type.APIKEY)
                                .in(SecurityScheme.In.COOKIE)
                                .name("taskmaster_token")
                                .description("HttpOnly cookie containing the JWT access token.")));
    }
}