package fpt.qn.mes.auth.integration;

import static org.assertj.core.api.Assertions.assertThat;

import java.lang.reflect.Method;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import fpt.qn.mes.AbstractIntegrationTest;
import fpt.qn.mes.auth.support.EndpointRoleCatalog;

class AuthorizationPolicyContractTest extends AbstractIntegrationTest {

    @Autowired
    @Qualifier("requestMappingHandlerMapping")
    RequestMappingHandlerMapping handlerMapping;

    @Test
    void authUserAndRoleHandlersFollowTheirAuthorizationContract() {
        int scopedHandlerCount = 0;
        int protectedHandlerCount = 0;
        Map<String, String> operations = new LinkedHashMap<>();
        for (var entry : handlerMapping.getHandlerMethods().entrySet()) {
            RequestMappingInfo mapping = entry.getKey();
            Set<String> paths = mapping.getPatternValues();
            if (paths.stream().noneMatch(this::isInAuthScope)) {
                continue;
            }
            for (String path : paths) {
                for (RequestMethod method : mapping.getMethodsCondition().getMethods()) {
                    String operation = method.name() + " " + path;
                    String access = annotationAccess(entry.getValue().getMethod());
                    assertThat(operations.put(operation, access))
                            .as("operation must be classified once: " + operation)
                            .isNull();
                }
            }
            scopedHandlerCount++;
            Method method = entry.getValue().getMethod();
            PreAuthorize annotation = method.getAnnotation(PreAuthorize.class);
            boolean publicAuth = paths.stream().allMatch(path ->
                    path.equals("/api/auth/login") || path.equals("/api/auth/refresh") || path.equals("/api/auth/logout"));
            if (publicAuth) {
                assertThat(annotation).isNull();
            } else {
                assertThat(annotation).isNotNull();
                assertThat(annotation.value()).isEqualTo("hasRole('ADMIN')");
                protectedHandlerCount++;
            }
        }
        assertThat(scopedHandlerCount).isEqualTo(16);

        assertThat(protectedHandlerCount).isEqualTo(13);
        assertThat(operations).containsExactlyInAnyOrderEntriesOf(
                EndpointRoleCatalog.operations());
    }

    private boolean isInAuthScope(String path) {
        return path.startsWith("/api/auth/")
                || path.startsWith("/api/users")
                || path.startsWith("/api/roles");
    }

    private String annotationAccess(Method method) {
        PreAuthorize annotation = method.getAnnotation(PreAuthorize.class);
        return annotation == null
                ? EndpointRoleCatalog.PUBLIC
                : EndpointRoleCatalog.ROLE_PROTECTED;
    }
}
