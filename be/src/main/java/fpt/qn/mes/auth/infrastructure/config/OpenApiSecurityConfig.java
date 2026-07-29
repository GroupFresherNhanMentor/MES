package fpt.qn.mes.auth.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springdoc.core.customizers.OperationCustomizer;

import io.swagger.v3.oas.models.security.SecurityRequirement;

@Configuration
public class OpenApiSecurityConfig {

    @Bean
    public OperationCustomizer rbacOperationSecurity() {
        return (operation, handlerMethod) -> {
            PreAuthorize preAuthorize = handlerMethod.getMethodAnnotation(PreAuthorize.class);
            if (preAuthorize != null) {
                operation.addSecurityItem(new SecurityRequirement().addList("bearerAuth"));
            }
            return operation;
        };
    }
}
