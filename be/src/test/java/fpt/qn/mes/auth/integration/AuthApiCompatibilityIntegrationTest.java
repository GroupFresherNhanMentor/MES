package fpt.qn.mes.auth.integration;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.TestRestTemplate;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import fpt.qn.mes.AbstractIntegrationTest;
import fpt.qn.mes.auth.application.dto.request.CreateRoleRequest;
import fpt.qn.mes.auth.application.dto.request.LoginRequest;
import fpt.qn.mes.auth.application.dto.request.RefreshRequest;
import fpt.qn.mes.auth.application.dto.request.UpdateRoleRequest;
import fpt.qn.mes.auth.application.dto.response.RoleDto;
import fpt.qn.mes.auth.application.dto.response.TokenResponse;
import fpt.qn.mes.common.dto.response.ApiResponse;
import fpt.qn.mes.user.application.dto.request.CreateUserRequest;
import fpt.qn.mes.user.application.dto.request.UpdateUserRequest;
import fpt.qn.mes.user.application.dto.response.UserDto;

class AuthApiCompatibilityIntegrationTest extends AbstractIntegrationTest {

    @Autowired TestRestTemplate restTemplate;
    @Autowired ObjectMapper objectMapper;

    @Test
    void authUserAndRoleOperationsAndDtoFieldsRemainGolden()
            throws Exception {
        Map<String, Integer> expectedOperations = new LinkedHashMap<>();
        expectedOperations.put("post /api/auth/login", 200);
        expectedOperations.put("post /api/auth/refresh", 200);
        expectedOperations.put("get /api/users", 200);
        expectedOperations.put("get /api/users/{id}", 200);
        expectedOperations.put("post /api/users", 201);
        expectedOperations.put("put /api/users/{id}", 200);
        expectedOperations.put("patch /api/users/{id}/activate", 200);
        expectedOperations.put("patch /api/users/{id}/deactivate", 200);
        expectedOperations.put("get /api/roles", 200);
        expectedOperations.put("get /api/roles/{id}", 200);
        expectedOperations.put("post /api/roles", 201);
        expectedOperations.put("put /api/roles/{id}", 200);
        expectedOperations.put("delete /api/roles/{id}", 200);

        JsonNode openApi = objectMapper.readTree(
                restTemplate.getForEntity("/v3/api-docs", String.class).getBody());
        for (Map.Entry<String, Integer> expected : expectedOperations.entrySet()) {
            String[] operation = expected.getKey().split(" ", 2);
            JsonNode contract = openApi.path("paths")
                    .path(operation[1])
                    .path(operation[0]);
            assertThat(contract.isMissingNode())
                    .as(expected.getKey())
                    .isFalse();
            assertThat(contract.path("responses").has(Integer.toString(expected.getValue())))
                    .as(expected.getKey() + " success status")
                    .isTrue();
        }
        assertThat(openApi.path("paths").has("/api/permissions")).isFalse();
        assertThat(openApi.path("paths").has("/api/permissions/{id}")).isFalse();
        assertThat(openApi.path("paths").has("/api/roles/{id}/permissions")).isFalse();

        assertFields(LoginRequest.class, "username", "password");
        assertFields(RefreshRequest.class, "refreshToken");
        assertFields(CreateUserRequest.class, "username", "password", "fullName");
        assertFields(UpdateUserRequest.class, "fullName");
        assertFields(CreateRoleRequest.class, "name", "description");
        assertFields(UpdateRoleRequest.class, "name", "description");

        assertFields(UserDto.class, "id", "username", "fullName", "active", "createdAt");
        assertFields(RoleDto.class, "id", "name", "description", "permissionNames");
        assertFields(
                ApiResponse.class,
                "success", "data", "errorCode", "message", "details", "timestamp");
        assertFields(
                TokenResponse.class,
                "userId", "username", "accessToken", "refreshToken");
    }

    private void assertFields(Class<?> type, String... expected) {
        assertThat(fields(type)).containsExactlyInAnyOrder(expected);
    }

    private Set<String> fields(Class<?> type) {
        Set<String> result = new LinkedHashSet<>();
        Arrays.stream(type.getDeclaredFields())
                .filter(field -> !field.isSynthetic())
                .map(field -> field.getName())
                .forEach(fieldName -> result.add(fieldName));
        return result;
    }
}
