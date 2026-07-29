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
import fpt.qn.mes.auth.application.permission.RolePermissionPolicy;
import fpt.qn.mes.auth.support.EndpointPermissionCatalog;

class AuthorizationPolicyContractTest extends AbstractIntegrationTest {

    @Autowired
    @Qualifier("requestMappingHandlerMapping")
    RequestMappingHandlerMapping handlerMapping;

    @Autowired
    RolePermissionPolicy rolePermissionPolicy;

    @Test
    void allNinetyOneApiHandlersArePublicAuthOrExactStaticRolePolicyProtected() {
        int apiHandlerCount = 0;
        int protectedHandlerCount = 0;
        Map<String, String> operations = new LinkedHashMap<>();
        for (var entry : handlerMapping.getHandlerMethods().entrySet()) {
            RequestMappingInfo mapping = entry.getKey();
            Set<String> paths = mapping.getPatternValues();
            if (paths.stream().noneMatch(path -> path.startsWith("/api/"))) {
                continue;
            }
            for (String path : paths) {
                for (RequestMethod method : mapping.getMethodsCondition().getMethods()) {
                    String operation = method.name() + " " + path;
                    String authority = annotationAuthority(entry.getValue().getMethod());
                    assertThat(operations.put(operation, authority))
                            .as("operation must be classified once: " + operation)
                            .isNull();
                }
            }
            apiHandlerCount++;
            Method method = entry.getValue().getMethod();
            PreAuthorize annotation = method.getAnnotation(PreAuthorize.class);
            boolean publicAuth = paths.stream().allMatch(path ->
                    path.equals("/api/auth/login") || path.equals("/api/auth/refresh"));
            if (publicAuth) {
                assertThat(annotation).isNull();
            } else {
                assertThat(annotation).isNotNull();
                assertThat(annotation.value())
                        .matches("hasAuthority\\('[A-Z][A-Z0-9]*_[A-Z][A-Z0-9_]*'\\)");
                assertThat(rolePermissionPolicy.allApiPermissions())
                        .contains(annotationAuthority(method));
                protectedHandlerCount++;
            }
        }
        assertThat(apiHandlerCount).isEqualTo(91);
        assertThat(protectedHandlerCount).isEqualTo(89);
        assertThat(operations).containsExactlyInAnyOrderEntriesOf(
                EndpointPermissionCatalog.operations());
    }

    private String annotationAuthority(Method method) {
        PreAuthorize annotation = method.getAnnotation(PreAuthorize.class);
        if (annotation == null) {
            return EndpointPermissionCatalog.PUBLIC;
        }
        String prefix = "hasAuthority('";
        return annotation.value().substring(prefix.length(), annotation.value().length() - 2);
    }
}
