package fpt.qn.mes.common.config;

import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;

@Configuration
public class OpenApiConfig {

    private static final String SECURITY_SCHEME_NAME = "bearerAuth";

    @Bean
    public OpenAPI customOpenAPI() {
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
    public GroupedOpenApi allApi() {
        return GroupedOpenApi.builder()
                .group("00. All Endpoints")
                .pathsToMatch("/api/**")
                .build();
    }

    @Bean
    public GroupedOpenApi authApi() {
        return GroupedOpenApi.builder()
                .group("01. Authentication")
                .pathsToMatch("/api/auth/**")
                .build();
    }

    @Bean
    public GroupedOpenApi masterDataApi() {
        return GroupedOpenApi.builder()
                .group("02. Master Data")
                .packagesToScan("fpt.qn.mes.master")
                .build();
    }

    @Bean
    public GroupedOpenApi inventoryApi() {
        return GroupedOpenApi.builder()
                .group("03. Inventory")
                .pathsToMatch("/api/stock-balances/**", "/api/stock-lots/**", "/api/stock-movements/**", "/api/lot-types/**", "/api/stock-statuses/**")
                .build();
    }

    @Bean
    public GroupedOpenApi bomApi() {
        return GroupedOpenApi.builder()
                .group("04. Bill of Materials (BOM)")
                .pathsToMatch("/api/boms/**")
                .build();
    }

    @Bean
    public GroupedOpenApi workOrderApi() {
        return GroupedOpenApi.builder()
                .group("05. Work Orders")
                .pathsToMatch("/api/work-orders/**")
                .build();
    }

    @Bean
    public GroupedOpenApi qualityApi() {
        return GroupedOpenApi.builder()
                .group("06. Quality Control")
                .pathsToMatch("/api/quality/**")
                .build();
    }

    @Bean
    public GroupedOpenApi maintenanceApi() {
        return GroupedOpenApi.builder()
                .group("07. Maintenance")
                .pathsToMatch("/api/maintenance/**")
                .build();
    }

    @Bean
    public GroupedOpenApi userSecurityApi() {
        return GroupedOpenApi.builder()
                .group("08. User & Access Control")
                .pathsToMatch("/api/users/**", "/api/roles/**", "/api/permissions/**")
                .build();
    }
}
