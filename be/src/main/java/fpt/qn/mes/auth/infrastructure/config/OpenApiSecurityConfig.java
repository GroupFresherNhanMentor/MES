package fpt.qn.mes.auth.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springdoc.core.customizers.OperationCustomizer;

import fpt.qn.mes.auth.presentation.AuthController;
import io.swagger.v3.oas.models.security.SecurityRequirement;

@Configuration
public class OpenApiSecurityConfig {

    @Bean
    public OperationCustomizer rbacOperationSecurity() {
        return (operation, handlerMethod) -> {
            boolean publicAuthOperation =
                    AuthController.class.isAssignableFrom(handlerMethod.getBeanType())
                            && ("login".equals(handlerMethod.getMethod().getName())
                                    || "refresh".equals(handlerMethod.getMethod().getName()));
            if (!publicAuthOperation) {
                operation.addSecurityItem(new SecurityRequirement().addList("bearerAuth"));
            }
            return operation;
        };
    }
}
