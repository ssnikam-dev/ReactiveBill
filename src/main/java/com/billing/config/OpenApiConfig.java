package com.billing.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/** Metadata shown at the top of Swagger UI (/swagger-ui.html). */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI billingOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("Reactive Billing API")
                        .version("0.1.0")
                        .description("""
                                Non-blocking invoice management API built with Spring WebFlux, \
                                Project Reactor and Spring Data R2DBC on PostgreSQL.

                                **Invoice lifecycle:** an invoice starts as `DRAFT` and can be paid or \
                                cancelled exactly once. `PAID` and `CANCELLED` are final: they cannot be \
                                edited, paid, cancelled or deleted again (`409 Conflict`).

                                Errors use RFC 7807 `application/problem+json`."""))
                .servers(List.of(new Server().url("/").description("Current server")));
    }
}
