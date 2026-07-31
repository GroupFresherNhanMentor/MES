package fpt.qn.mes.common.config;

import org.springdoc.core.customizers.OperationCustomizer;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.media.StringSchema;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;

@Configuration
public class OpenApiConfig {

    private static final String SECURITY_SCHEME_NAME = "bearerAuth";

    @Bean
    OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("MES — Manufacturing Execution System API")
                        .version("1.0.0")
                        .description("API documentation for the Manufacturing Execution System (Clean Architecture, Spring Boot, jOOQ)")
                        .contact(new Contact().name("FPT MES Team")))
                .addSecurityItem(new SecurityRequirement().addList(SECURITY_SCHEME_NAME))
                .components(new Components()
                        .addSecuritySchemes(SECURITY_SCHEME_NAME,
                                new SecurityScheme()
                                        .name(SECURITY_SCHEME_NAME)
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")));
    }

    @Bean
    OperationCustomizer idempotencyKeyHeaderCustomizer() {
        return (operation, handlerMethod) -> {
            operation.addParametersItem(new Parameter()
                    .in("header")
                    .name("X-Idempotency-Key")
                    .description("Optional unique key to prevent duplicate request execution")
                    .required(false)
                    .schema(new StringSchema()));
            return operation;
        };
    }

    @Bean
    GroupedOpenApi allApi(OperationCustomizer idempotencyKeyHeaderCustomizer) {
        return GroupedOpenApi.builder()
                .group("00. All Endpoints")
                .pathsToMatch("/api/**")
                .addOperationCustomizer(idempotencyKeyHeaderCustomizer) // <-- THÊM VÀO ĐÂY
                .build();
    }

    @Bean
    GroupedOpenApi authApi(OperationCustomizer idempotencyKeyHeaderCustomizer) {
        return GroupedOpenApi.builder()
                .group("01. Authentication")
                .pathsToMatch("/api/auth/**")
                .addOperationCustomizer(idempotencyKeyHeaderCustomizer) // <-- THÊM VÀO ĐÂY
                .build();
    }

    @Bean
    GroupedOpenApi masterDataApi(OperationCustomizer idempotencyKeyHeaderCustomizer) {
        return GroupedOpenApi.builder()
                .group("02. Master Data")
                .packagesToScan("fpt.qn.mes.master")
                .addOperationCustomizer(idempotencyKeyHeaderCustomizer) // <-- THÊM VÀO ĐÂY
                .build();
    }

    @Bean
    GroupedOpenApi inventoryApi(OperationCustomizer idempotencyKeyHeaderCustomizer) {
        return GroupedOpenApi.builder()
                .group("03. Inventory")
                .pathsToMatch("/api/stock**", "/api/movement-types/**", "/api/lot-types/**", "/api/stock**/**")
                .addOperationCustomizer(idempotencyKeyHeaderCustomizer) // <-- THÊM VÀO ĐÂY
                .build();
    }

    @Bean
    GroupedOpenApi bomApi(OperationCustomizer idempotencyKeyHeaderCustomizer) {
        return GroupedOpenApi.builder()
                .group("04. Bill of Materials (BOM)")
                .pathsToMatch("/api/boms/**")
                .addOperationCustomizer(idempotencyKeyHeaderCustomizer) // <-- THÊM VÀO ĐÂY
                .build();
    }

    @Bean
    GroupedOpenApi workOrderApi(OperationCustomizer idempotencyKeyHeaderCustomizer) {
        return GroupedOpenApi.builder()
                .group("05. Work Orders")
                .pathsToMatch("/api/work-orders/**")
                .addOperationCustomizer(idempotencyKeyHeaderCustomizer) // <-- THÊM VÀO ĐÂY
                .build();
    }

    @Bean
    GroupedOpenApi qualityApi(OperationCustomizer idempotencyKeyHeaderCustomizer) {
        return GroupedOpenApi.builder()
                .group("06. Quality Control")
                .pathsToMatch("/api/quality/**")
                .addOperationCustomizer(idempotencyKeyHeaderCustomizer) // <-- THÊM VÀO ĐÂY
                .build();
    }

    @Bean
    GroupedOpenApi maintenanceApi(OperationCustomizer idempotencyKeyHeaderCustomizer) {
        return GroupedOpenApi.builder()
                .group("07. Maintenance")
                .pathsToMatch("/api/maintenance/**")
                .addOperationCustomizer(idempotencyKeyHeaderCustomizer) // <-- THÊM VÀO ĐÂY
                .build();
    }

    @Bean
    GroupedOpenApi userSecurityApi(OperationCustomizer idempotencyKeyHeaderCustomizer) {
        return GroupedOpenApi.builder()
                .group("08. User & Access Control")
                .pathsToMatch("/api/users/**", "/api/roles/**", "/api/permissions/**")
                .addOperationCustomizer(idempotencyKeyHeaderCustomizer) // <-- THÊM VÀO ĐÂY
                .build();
    }
}