package fpt.qn.mes.auth.integration;

import static org.assertj.core.api.Assertions.assertThat;

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
import fpt.qn.mes.jooq.Tables;

class AuthorizationIntegrationTest extends AbstractIntegrationTest {

    @Autowired TestRestTemplate restTemplate;
    @Autowired ObjectMapper objectMapper;
    @Autowired AuthTestData authTestData;
    @Autowired DSLContext ctx;

    @Test
    void requestReloadsMultiRolePolicyAndActiveStateFromDatabase() throws Exception {
        String suffix = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        var identity = authTestData.createIdentity(
                "authorization-" + suffix.toLowerCase(),
                "Password@123",
                true,
                List.of("AUDITOR", "OPERATOR"),
                List.of("IGNORED_DATABASE_PERMISSION"));
        UUID adminRoleId = ctx.select(Tables.ROLES.ID)
                .from(Tables.ROLES)
                .where(Tables.ROLES.NAME.eq("ADMIN"))
                .fetchOne(Tables.ROLES.ID);
        try {
            ResponseEntity<String> anonymous = restTemplate.getForEntity("/api/users", String.class);
            assertThat(anonymous.getStatusCode().value()).isEqualTo(401);

            JsonNode tokens = login(identity.getUsername(), identity.getRawPassword());
            assertThat(tokens.path("userId").asText())
                    .isEqualTo(identity.getUserId().toString());
            assertThat(tokens.path("username").asText())
                    .isEqualTo(identity.getUsername());
            String accessToken = tokens.path("accessToken").asText();
            assertThat(authorized("/api/stock-balances", accessToken)
                    .getStatusCode().value()).isEqualTo(200);
            assertThat(authorized("/api/work-orders", accessToken)
                    .getStatusCode().value()).isEqualTo(200);
            assertThat(authorized("/api/users", accessToken)
                    .getStatusCode().value()).isEqualTo(403);

            ctx.insertInto(Tables.USER_ROLES)
                    .values(identity.getUserId(), adminRoleId)
                    .execute();
            assertThat(authorized("/api/users", accessToken)
                    .getStatusCode().value()).isEqualTo(200);

            ctx.deleteFrom(Tables.USER_ROLES)
                    .where(Tables.USER_ROLES.USER_ID.eq(identity.getUserId()))
                    .and(Tables.USER_ROLES.ROLE_ID.eq(adminRoleId))
                    .execute();
            assertThat(authorized("/api/users", accessToken)
                    .getStatusCode().value()).isEqualTo(403);

            ctx.update(Tables.USERS).set(Tables.USERS.ACTIVE, false)
                    .where(Tables.USERS.ID.eq(identity.getUserId())).execute();
            assertThat(authorized("/api/users", accessToken)
                    .getStatusCode().value()).isEqualTo(401);
            assertThat(authorized("/api/users", tokens.path("refreshToken").asText())
                    .getStatusCode().value()).isEqualTo(401);
        } finally {
            authTestData.deleteIdentity(identity);
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

    private ResponseEntity<String> authorized(String path, String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        return restTemplate.exchange(
                path,
                HttpMethod.GET,
                new HttpEntity<>(null, headers),
                String.class);
    }
}
