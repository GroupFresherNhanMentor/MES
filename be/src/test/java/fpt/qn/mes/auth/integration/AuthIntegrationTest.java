package fpt.qn.mes.auth.integration;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.time.Duration;

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

class AuthIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    TestRestTemplate restTemplate;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    AuthTestData authTestData;

    @Test
    void loginAndStatelessRefreshReturnIdentityAndTokensOnly() throws Exception {
        String suffix = UUID.randomUUID().toString().substring(0, 8);
        var identity = authTestData.createIdentity(
                "auth-" + suffix,
                "Password@123",
                true,
                List.of(
                        "ADMIN",
                        "AUDITOR"),
                List.of());
        try {
            JsonNode login = post("/api/auth/login", Map.of(
                    "username", identity.getUsername(),
                    "password", identity.getRawPassword()));
            assertThat(login.path("success").asBoolean()).isTrue();
            JsonNode loginData = login.path("data");
            assertThat(loginData.size()).isEqualTo(4);
            assertThat(loginData.path("userId").asText())
                    .isEqualTo(identity.getUserId().toString());
            assertThat(loginData.path("username").asText())
                    .isEqualTo(identity.getUsername());
            assertThat(loginData.path("accessToken").asText()).isNotBlank();
            assertThat(loginData.path("refreshToken").asText()).isNotBlank();
            assertThat(loginData.has("userProfile")).isFalse();
            assertThat(loginData.has("roles")).isFalse();
            assertThat(loginData.has("permissions")).isFalse();
            assertThat(login.toString()).doesNotContain("passwordHash");

            ResponseEntity<String> allowed = authorized(
                    "/api/users", HttpMethod.GET, null, loginData.path("accessToken").asText());
            assertThat(allowed.getStatusCode().value()).isEqualTo(200);

            JsonNode refreshed = post("/api/auth/refresh", Map.of(
                    "refreshToken", loginData.path("refreshToken").asText()));
            assertThat(refreshed.path("success").asBoolean()).isTrue();
            JsonNode refreshedData = refreshed.path("data");
            assertThat(refreshedData.size()).isEqualTo(4);
            assertThat(refreshedData.path("userId").asText())
                    .isEqualTo(identity.getUserId().toString());
            assertThat(refreshedData.path("username").asText())
                    .isEqualTo(identity.getUsername());
            assertThat(refreshedData.path("refreshToken").asText())
                    .isNotEqualTo(loginData.path("refreshToken").asText());

            ResponseEntity<String> reusedOriginal = restTemplate.postForEntity(
                    "/api/auth/refresh",
                    Map.of("refreshToken", loginData.path("refreshToken").asText()),
                    String.class);
            assertThat(reusedOriginal.getStatusCode().value()).isEqualTo(200);

            ResponseEntity<String> successor = restTemplate.postForEntity(
                    "/api/auth/refresh",
                    Map.of("refreshToken", refreshedData.path("refreshToken").asText()),
                    String.class);
            assertThat(successor.getStatusCode().value()).isEqualTo(200);

            ResponseEntity<String> refreshedAccess = authorized(
                    "/api/users",
                    HttpMethod.GET,
                    null,
                    refreshedData.path("accessToken").asText());
            assertThat(refreshedAccess.getStatusCode().value()).isEqualTo(200);
        } finally {
            authTestData.deleteIdentity(identity);
        }
    }

    @Test
    void invalidCredentialsReturnGenericUnauthorized() throws Exception {
        ResponseEntity<String> response = restTemplate.postForEntity(
                "/api/auth/login",
                Map.of("username", "missing-" + UUID.randomUUID(), "password", "wrong-password"),
                String.class);
        assertThat(response.getStatusCode().value()).isEqualTo(401);
        JsonNode body = objectMapper.readTree(response.getBody());
        assertThat(body.path("message").asText()).isEqualTo("Invalid username or password");
        assertThat(response.getBody()).doesNotContain("missing user", "passwordHash", "BCrypt");
    }

    @Test
    void inactiveUsersAndTokensUsedForTheWrongPurposeAreRejected() throws Exception {
        String suffix = UUID.randomUUID().toString().substring(0, 8);
        var inactive = authTestData.createIdentity(
                "inactive-" + suffix,
                "Password@123",
                false,
                List.of(),
                List.of());
        var active = authTestData.createIdentity(
                "purpose-" + suffix,
                "Password@123",
                true,
                List.of("ADMIN"),
                List.of());
        try {
            ResponseEntity<String> inactiveLogin = restTemplate.postForEntity(
                    "/api/auth/login",
                    Map.of(
                            "username", inactive.getUsername(),
                            "password", inactive.getRawPassword()),
                    String.class);
            assertThat(inactiveLogin.getStatusCode().value()).isEqualTo(401);
            assertThat(objectMapper.readTree(inactiveLogin.getBody()).path("message").asText())
                    .isEqualTo("Invalid username or password");

            JsonNode tokens = post("/api/auth/login", Map.of(
                    "username", active.getUsername(),
                    "password", active.getRawPassword())).path("data");
            ResponseEntity<String> refreshAsAccess = authorized(
                    "/api/users",
                    HttpMethod.GET,
                    null,
                    tokens.path("refreshToken").asText());
            assertThat(refreshAsAccess.getStatusCode().value()).isEqualTo(401);

            ResponseEntity<String> accessAsRefresh = restTemplate.postForEntity(
                    "/api/auth/refresh",
                    Map.of("refreshToken", tokens.path("accessToken").asText()),
                    String.class);
            assertThat(accessAsRefresh.getStatusCode().value()).isEqualTo(401);

            ResponseEntity<String> expiredAccess = authorized(
                    "/api/users",
                    HttpMethod.GET,
                    null,
                    generateExpiredToken(
                            active.getUserId(),
                            active.getUsername()));
            assertThat(expiredAccess.getStatusCode().value()).isEqualTo(401);

            ResponseEntity<String> malformedRefresh = restTemplate.postForEntity(
                    "/api/auth/refresh",
                    Map.of("refreshToken", "not-a-jwt"),
                    String.class);
            assertThat(malformedRefresh.getStatusCode().value()).isEqualTo(401);
        } finally {
            authTestData.deleteIdentity(inactive);
            authTestData.deleteIdentity(active);
        }
    }

    @Test
    void concurrentDoubleRefreshSucceedsWithoutServerSideTokenState() throws Exception {
        String suffix = UUID.randomUUID().toString().substring(0, 8);
        var identity = authTestData.createIdentity(
                "refresh-" + suffix,
                "Password@123",
                true,
                List.of("ADMIN"),
                List.of());
        try {
            JsonNode login = post("/api/auth/login", Map.of(
                    "username", identity.getUsername(),
                    "password", identity.getRawPassword())).path("data");
            String refreshToken = login.path("refreshToken").asText();
            ConcurrentRequestRunner runner = new ConcurrentRequestRunner();
            List<Integer> statuses = runner.run(
                    2,
                    () -> restTemplate.postForEntity(
                            "/api/auth/refresh",
                            Map.of("refreshToken", refreshToken),
                            String.class).getStatusCode().value(),
                    Duration.ofSeconds(10));
            assertThat(statuses).containsOnly(200).hasSize(2);

            ResponseEntity<String> oldAccess = authorized(
                    "/api/users", HttpMethod.GET, null, login.path("accessToken").asText());
            assertThat(oldAccess.getStatusCode().value()).isEqualTo(200);
        } finally {
            authTestData.deleteIdentity(identity);
        }
    }

    @Test
    void repeatedFailedLoginsStayUnauthorizedAndDoNotBlockCorrectCredentials() {
        String suffix = UUID.randomUUID().toString().substring(0, 8);
        var identity = authTestData.createIdentity(
                "throttle-" + suffix,
                "Password@123",
                true,
                List.of(),
                List.of());
        try {
            for (int attempt = 1; attempt <= 6; attempt++) {
                ResponseEntity<String> response = restTemplate.postForEntity(
                        "/api/auth/login",
                        Map.of("username", identity.getUsername(), "password", "wrong-password"),
                        String.class);
                assertThat(response.getStatusCode().value()).isEqualTo(401);
            }
            ResponseEntity<String> correct = restTemplate.postForEntity(
                    "/api/auth/login",
                    Map.of("username", identity.getUsername(), "password", identity.getRawPassword()),
                    String.class);
            assertThat(correct.getStatusCode().value()).isEqualTo(200);
            assertThat(correct.getHeaders().getFirst("Retry-After")).isNull();
        } finally {
            authTestData.deleteIdentity(identity);
        }
    }

    private JsonNode post(String path, Object body) throws Exception {
        ResponseEntity<String> response = restTemplate.postForEntity(path, body, String.class);
        assertThat(response.getStatusCode().value()).isEqualTo(200);
        return objectMapper.readTree(response.getBody());
    }

    private ResponseEntity<String> authorized(
            String path,
            HttpMethod method,
            Object body,
            String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        return restTemplate.exchange(path, method, new HttpEntity<>(body, headers), String.class);
    }
}
