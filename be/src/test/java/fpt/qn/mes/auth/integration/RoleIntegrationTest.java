package fpt.qn.mes.auth.integration;

import static fpt.qn.mes.jooq.Tables.ROLES;
import static fpt.qn.mes.jooq.Tables.USERS;
import static fpt.qn.mes.jooq.Tables.USER_ROLES;
import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.jooq.DSLContext;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import fpt.qn.mes.AbstractIntegrationTest;
import fpt.qn.mes.auth.support.AuthTestData;
import fpt.qn.mes.auth.support.ConcurrentRequestRunner;

class RoleIntegrationTest extends AbstractIntegrationTest {

    @Autowired TestRestTemplate restTemplate;
    @Autowired ObjectMapper objectMapper;
    @Autowired DSLContext ctx;
    @Autowired AuthTestData authTestData;

    @Test
    void roleCrudReturnsOnlyRoleData() throws Exception {
        String suffix = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        String token = loginAdmin();
        UUID roleId = null;
        UUID assignedUserId = null;
        try {
            JsonNode created = exchangeJson(
                    "/api/roles",
                    HttpMethod.POST,
                    Map.of("name", " role_" + suffix + " ", "description", "Role test"),
                    token,
                    201).path("data");
            roleId = UUID.fromString(created.path("id").asText());
            assertThat(created.path("name").asText()).isEqualTo("ROLE_" + suffix);

            JsonNode roleList = exchangeJson(
                    "/api/roles", HttpMethod.GET, null, token, 200).path("data");
            List<String> roleNames = new ArrayList<>();
            roleList.forEach(role -> roleNames.add(role.path("name").asText()));
            assertThat(roleNames).isSorted();

            JsonNode updated = exchangeJson(
                    "/api/roles/" + roleId,
                    HttpMethod.PUT,
                    Map.of("name", "updated_" + suffix, "description", "Updated"),
                    token,
                    200).path("data");
            assertThat(updated.path("name").asText()).isEqualTo("UPDATED_" + suffix);

            assignedUserId = UUID.randomUUID();
            ctx.insertInto(USERS)
                    .set(USERS.ID, assignedUserId)
                    .set(USERS.USERNAME, "role-assigned-" + suffix.toLowerCase())
                    .set(USERS.PASSWORD_HASH, "$2a$10$abcdefghijklmnopqrstuu1234567890123456789012345678901")
                    .set(USERS.ACTIVE, true)
                    .set(USERS.CREATED_AT, java.time.OffsetDateTime.now())
                    .execute();
            ctx.insertInto(USER_ROLES).values(assignedUserId, roleId).execute();
            assertThat(exchange("/api/roles/" + roleId, HttpMethod.DELETE, null, token)
                    .getStatusCode().value()).isEqualTo(409);

            ctx.deleteFrom(USERS).where(USERS.ID.eq(assignedUserId)).execute();
            assignedUserId = null;
            assertThat(exchange("/api/roles/" + roleId, HttpMethod.DELETE, null, token)
                    .getStatusCode().value()).isEqualTo(200);
            roleId = null;
        } finally {
            if (assignedUserId != null) {
                ctx.deleteFrom(USERS).where(USERS.ID.eq(assignedUserId)).execute();
            }
            if (roleId != null) {
                ctx.deleteFrom(ROLES).where(ROLES.ID.eq(roleId)).execute();
            }
        }
    }

    @Test
    void roleEndpointsEnforceAuthenticationAuthorizationAndErrors() throws Exception {
        String suffix = UUID.randomUUID().toString().substring(0, 8);
        var limited = authTestData.createIdentity(
                "role-limited-" + suffix,
                "Password@123",
                true,
                List.of("AUDITOR"));
        try {
            assertThat(exchange("/api/roles", HttpMethod.GET, null, null)
                    .getStatusCode().value()).isEqualTo(401);
            assertThat(exchange(
                    "/api/roles",
                    HttpMethod.GET,
                    null,
                    loginAccess(limited.getUsername(), limited.getRawPassword()))
                    .getStatusCode().value()).isEqualTo(403);
            assertThat(exchange(
                    "/api/roles",
                    HttpMethod.POST,
                    Map.of("name", " ", "description", "Invalid"),
                    loginAdmin())
                    .getStatusCode().value()).isEqualTo(400);
            assertThat(exchange(
                    "/api/roles/" + UUID.randomUUID(),
                    HttpMethod.GET,
                    null,
                    loginAdmin())
                    .getStatusCode().value()).isEqualTo(404);
        } finally {
            authTestData.deleteIdentity(limited);
        }
    }

    @Test
    void concurrentNormalizedCreationAllowsExactlyOneRole() throws Exception {
        String suffix = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        String roleName = "ROLE_RACE_" + suffix;
        String token = loginAdmin();
        UUID roleId = null;
        try {
            ConcurrentRequestRunner runner = new ConcurrentRequestRunner();
            List<Integer> statuses = runner.run(
                    List.of(
                            () -> exchange(
                                    "/api/roles",
                                    HttpMethod.POST,
                                    Map.of("name", " " + roleName.toLowerCase() + " ",
                                            "description", "Race role"),
                                    token).getStatusCode().value(),
                            () -> exchange(
                                    "/api/roles",
                                    HttpMethod.POST,
                                    Map.of("name", roleName, "description", "Race role"),
                                    token).getStatusCode().value()),
                    Duration.ofSeconds(15));
            assertThat(statuses).containsExactlyInAnyOrder(201, 409);
            roleId = ctx.select(ROLES.ID).from(ROLES)
                    .where(ROLES.NAME.eq(roleName)).fetchOne(ROLES.ID);
        } finally {
            if (roleId != null) {
                ctx.deleteFrom(ROLES).where(ROLES.ID.eq(roleId)).execute();
            }
        }
    }

    private String loginAdmin() throws Exception {
        return loginAccess("admin", "Admin@1234");
    }

    private String loginAccess(String username, String password) throws Exception {
        ResponseEntity<String> response = restTemplate.postForEntity(
                "/api/auth/login",
                Map.of("username", username, "password", password),
                String.class);
        return objectMapper.readTree(response.getBody()).path("data").path("accessToken").asText();
    }

    private JsonNode exchangeJson(
            String path, HttpMethod method, Object body, String token, int expected) throws Exception {
        ResponseEntity<String> response = exchange(path, method, body, token);
        assertThat(response.getStatusCode().value()).isEqualTo(expected);
        return objectMapper.readTree(response.getBody());
    }

    private ResponseEntity<String> exchange(
            String path, HttpMethod method, Object body, String token) {
        HttpHeaders headers = new HttpHeaders();
        if (token != null) {
            headers.setBearerAuth(token);
        }
        return restTemplate.exchange(path, method, new HttpEntity<>(body, headers), String.class);
    }
}
