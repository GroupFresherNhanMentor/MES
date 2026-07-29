package fpt.qn.mes.user.integration;

import static fpt.qn.mes.jooq.Tables.ROLES;
import static fpt.qn.mes.jooq.Tables.USERS;
import static fpt.qn.mes.jooq.Tables.USER_ROLES;
import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;
import java.time.Instant;
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

class UserIntegrationTest extends AbstractIntegrationTest {

    @Autowired TestRestTemplate restTemplate;
    @Autowired ObjectMapper objectMapper;
    @Autowired DSLContext ctx;
    @Autowired AuthTestData authTestData;

    @Test
    void completeUserCrudAndRoleReplacementFlowMeetsSrsContract() throws Exception {
        String suffix = UUID.randomUUID().toString().substring(0, 8);
        String username = "user-flow-" + suffix;
        String token = login("admin", "Admin@1234").path("accessToken").asText();
        UUID userId = null;
        Instant started = Instant.now();
        try {
            JsonNode created = exchangeJson(
                    "/api/users",
                    HttpMethod.POST,
                    Map.of(
                            "username", username,
                            "password", "Password@123",
                            "fullName", "Original Name"),
                    token,
                    201).path("data");
            userId = UUID.fromString(created.path("id").asText());
            assertThat(created.has("password")).isFalse();
            assertThat(created.has("passwordHash")).isFalse();
            assertThat(exchange(
                    "/api/users",
                    HttpMethod.POST,
                    Map.of(
                            "username", username,
                            "password", "Password@123",
                            "fullName", "Duplicate"),
                    token).getStatusCode().value()).isEqualTo(409);
            JsonNode page = exchangeJson(
                    "/api/users?page=0&size=20",
                    HttpMethod.GET,
                    null,
                    token,
                    200).path("data");
            assertThat(page.path("items").isArray()).isTrue();

            JsonNode detail = exchangeJson(
                    "/api/users/" + userId, HttpMethod.GET, null, token, 200).path("data");
            assertThat(detail.path("username").asText()).isEqualTo(username);
            JsonNode updated = exchangeJson(
                    "/api/users/" + userId,
                    HttpMethod.PUT,
                    Map.of("fullName", "Updated Name"),
                    token,
                    200).path("data");
            assertThat(updated.path("fullName").asText()).isEqualTo("Updated Name");

            UUID auditorRole = ctx.select(ROLES.ID).from(ROLES)
                    .where(ROLES.NAME.eq("AUDITOR")).fetchOne(ROLES.ID);
            JsonNode roles = exchangeJson(
                    "/api/users/" + userId + "/roles",
                    HttpMethod.PUT,
                    Map.of("roleIds", List.of(auditorRole, auditorRole)),
                    token,
                    200).path("data");
            assertThat(roles).hasSize(1);
            assertThat(Duration.between(started, Instant.now())).isLessThan(Duration.ofMinutes(3));

            ResponseEntity<String> invalid = exchange(
                    "/api/users/" + userId + "/roles",
                    HttpMethod.PUT,
                    Map.of("roleIds", List.of(auditorRole, UUID.randomUUID())),
                    token);
            assertThat(invalid.getStatusCode().value()).isEqualTo(404);
            assertThat(exchangeJson(
                    "/api/users/" + userId + "/roles",
                    HttpMethod.GET,
                    null,
                    token,
                    200).path("data")).hasSize(1);

            assertThat(exchange(
                    "/api/users/" + userId + "/deactivate",
                    HttpMethod.PATCH,
                    null,
                    token).getStatusCode().value()).isEqualTo(200);
            assertThat(restTemplate.postForEntity(
                    "/api/auth/login",
                    Map.of("username", username, "password", "Password@123"),
                    String.class).getStatusCode().value()).isEqualTo(401);
            assertThat(exchange(
                    "/api/users/" + userId + "/activate",
                    HttpMethod.PATCH,
                    null,
                    token).getStatusCode().value()).isEqualTo(200);

            UUID adminId = ctx.select(USERS.ID).from(USERS)
                    .where(USERS.USERNAME.eq("admin")).fetchOne(USERS.ID);
            assertThat(exchange(
                    "/api/users/" + adminId + "/deactivate",
                    HttpMethod.PATCH,
                    null,
                    token).getStatusCode().value()).isEqualTo(409);
        } finally {
            if (userId != null) {
                ctx.deleteFrom(USERS).where(USERS.ID.eq(userId)).execute();
            }
        }
    }

    @Test
    void userEndpointsEnforceAuthenticationAuthorizationValidationAndNotFound() {
        String suffix = UUID.randomUUID().toString().substring(0, 8);
        var limited = authTestData.createIdentity(
                "user-limited-" + suffix,
                "Password@123",
                true,
                List.of("USER_LIMITED_" + suffix.toUpperCase()),
                List.of("PRODUCT_READ"));
        try {
            assertThat(exchange("/api/users", HttpMethod.GET, null, null)
                    .getStatusCode().value()).isEqualTo(401);
            assertThat(exchange(
                    "/api/users",
                    HttpMethod.GET,
                    null,
                    loginUnchecked(limited.getUsername(), limited.getRawPassword())
                            .path("accessToken").asText())
                    .getStatusCode().value()).isEqualTo(403);
            String adminToken = loginUnchecked("admin", "Admin@1234")
                    .path("accessToken").asText();
            assertThat(exchange(
                    "/api/users",
                    HttpMethod.POST,
                    Map.of(
                            "username", " ",
                            "password", "short",
                            "fullName", "Invalid"),
                    adminToken).getStatusCode().value()).isEqualTo(400);
            assertThat(exchange(
                    "/api/users/" + UUID.randomUUID(),
                    HttpMethod.GET,
                    null,
                    adminToken).getStatusCode().value()).isEqualTo(404);
        } finally {
            authTestData.deleteIdentity(limited);
        }
    }

    @Test
    void concurrentDuplicateUsernameAndRoleReplacementsRemainAtomic() throws Exception {
        String suffix = UUID.randomUUID().toString().substring(0, 8);
        String username = "user-race-" + suffix;
        String token = login("admin", "Admin@1234").path("accessToken").asText();
        UUID firstRoleId = UUID.randomUUID();
        UUID secondRoleId = UUID.randomUUID();
        UUID userId = null;
        ctx.insertInto(ROLES)
                .columns(ROLES.ID, ROLES.NAME, ROLES.DESCRIPTION)
                .values(firstRoleId, "RACE_A_" + suffix.toUpperCase(), "Race role A")
                .values(secondRoleId, "RACE_B_" + suffix.toUpperCase(), "Race role B")
                .execute();
        try {
            ConcurrentRequestRunner runner = new ConcurrentRequestRunner();
            List<Integer> createStatuses = runner.run(
                    List.of(
                            () -> exchange(
                                    "/api/users",
                                    HttpMethod.POST,
                                    Map.of(
                                            "username", username,
                                            "password", "Password@123",
                                            "fullName", "Race User"),
                                    token).getStatusCode().value(),
                            () -> exchange(
                                    "/api/users",
                                    HttpMethod.POST,
                                    Map.of(
                                            "username", username,
                                            "password", "Password@123",
                                            "fullName", "Race User"),
                                    token).getStatusCode().value()),
                    Duration.ofSeconds(15));
            assertThat(createStatuses).containsExactlyInAnyOrder(201, 409);

            userId = ctx.select(USERS.ID).from(USERS)
                    .where(USERS.USERNAME.eq(username)).fetchOne(USERS.ID);
            UUID assignedUserId = userId;
            List<Integer> replacementStatuses = runner.run(
                    List.of(
                            () -> exchange(
                                    "/api/users/" + assignedUserId + "/roles",
                                    HttpMethod.PUT,
                                    Map.of("roleIds", List.of(firstRoleId)),
                                    token).getStatusCode().value(),
                            () -> exchange(
                                    "/api/users/" + assignedUserId + "/roles",
                                    HttpMethod.PUT,
                                    Map.of("roleIds", List.of(secondRoleId)),
                                    token).getStatusCode().value()),
                    Duration.ofSeconds(15));
            assertThat(replacementStatuses).containsOnly(200);
            List<UUID> finalRoleIds = ctx.select(USER_ROLES.ROLE_ID)
                    .from(USER_ROLES)
                    .where(USER_ROLES.USER_ID.eq(userId))
                    .fetch(USER_ROLES.ROLE_ID);
            assertThat(finalRoleIds).hasSize(1);
            assertThat(finalRoleIds.getFirst()).isIn(firstRoleId, secondRoleId);
        } finally {
            if (userId != null) {
                ctx.deleteFrom(USERS).where(USERS.ID.eq(userId)).execute();
            }
            ctx.deleteFrom(ROLES)
                    .where(ROLES.ID.in(firstRoleId, secondRoleId))
                    .execute();
        }
    }

    @Test
    void concurrentDeactivationAssignmentAndFinalAdminRemovalAreSerialized() throws Exception {
        String suffix = UUID.randomUUID().toString().substring(0, 8);
        var assignmentTarget = authTestData.createIdentity(
                "assignment-race-" + suffix,
                "Password@123",
                true,
                List.of(),
                List.of());
        var secondAdmin = authTestData.createIdentity(
                "second-admin-" + suffix,
                "Password@123",
                true,
                List.of("ADMIN"),
                List.of());
        UUID seededAdminId = ctx.select(USERS.ID).from(USERS)
                .where(USERS.USERNAME.eq("admin")).fetchOne(USERS.ID);
        String seededAdminToken = login("admin", "Admin@1234").path("accessToken").asText();
        String secondAdminToken = login(
                secondAdmin.getUsername(),
                secondAdmin.getRawPassword()).path("accessToken").asText();
        try {
            ConcurrentRequestRunner runner = new ConcurrentRequestRunner();
            UUID auditorRoleId = ctx.select(ROLES.ID).from(ROLES)
                    .where(ROLES.NAME.eq("AUDITOR")).fetchOne(ROLES.ID);
            List<Integer> assignmentRaceStatuses = runner.run(
                    List.of(
                            () -> exchange(
                                    "/api/users/" + assignmentTarget.getUserId() + "/deactivate",
                                    HttpMethod.PATCH,
                                    null,
                                    seededAdminToken).getStatusCode().value(),
                            () -> exchange(
                                    "/api/users/" + assignmentTarget.getUserId() + "/roles",
                                    HttpMethod.PUT,
                                    Map.of("roleIds", List.of(auditorRoleId)),
                                    seededAdminToken).getStatusCode().value()),
                    Duration.ofSeconds(15));
            assertThat(assignmentRaceStatuses).containsOnly(200);
            assertThat(ctx.select(USERS.ACTIVE).from(USERS)
                    .where(USERS.ID.eq(assignmentTarget.getUserId()))
                    .fetchOne(USERS.ACTIVE)).isFalse();
            assertThat(ctx.fetchExists(ctx.selectOne().from(USER_ROLES)
                    .where(USER_ROLES.USER_ID.eq(assignmentTarget.getUserId()))
                    .and(USER_ROLES.ROLE_ID.eq(auditorRoleId)))).isTrue();

            List<Integer> statuses = runner.run(
                    List.of(
                            () -> exchange(
                                    "/api/users/" + secondAdmin.getUserId() + "/deactivate",
                                    HttpMethod.PATCH,
                                    null,
                                    seededAdminToken).getStatusCode().value(),
                            () -> exchange(
                                    "/api/users/" + seededAdminId + "/deactivate",
                                    HttpMethod.PATCH,
                                    null,
                                    secondAdminToken).getStatusCode().value()),
                    Duration.ofSeconds(15));
            assertThat(statuses).contains(200);
            assertThat(statuses).allMatch(status -> status == 200 || status == 401 || status == 409);
            int activeAdministrators = ctx.fetchCount(
                    USERS,
                    USERS.ID.in(seededAdminId, secondAdmin.getUserId())
                            .and(USERS.ACTIVE.isTrue()));
            assertThat(activeAdministrators).isEqualTo(1);
        } finally {
            ctx.update(USERS)
                    .set(USERS.ACTIVE, true)
                    .where(USERS.ID.eq(seededAdminId))
                    .execute();
            authTestData.deleteIdentity(assignmentTarget);
            authTestData.deleteIdentity(secondAdmin);
        }
    }

    private JsonNode login(String username, String password) throws Exception {
        ResponseEntity<String> response = restTemplate.postForEntity(
                "/api/auth/login",
                Map.of("username", username, "password", password),
                String.class);
        assertThat(response.getStatusCode().value()).isEqualTo(200);
        return objectMapper.readTree(response.getBody()).path("data");
    }

    private JsonNode loginUnchecked(String username, String password) {
        try {
            return login(username, password);
        } catch (Exception exception) {
            throw new IllegalStateException("Cannot log in test identity", exception);
        }
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
