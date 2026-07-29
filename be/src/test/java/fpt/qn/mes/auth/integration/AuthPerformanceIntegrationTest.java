package fpt.qn.mes.auth.integration;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import org.jooq.ExecuteContext;
import org.jooq.impl.DefaultExecuteListener;
import org.jooq.impl.DefaultExecuteListenerProvider;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jooq.autoconfigure.DefaultConfigurationCustomizer;
import org.springframework.boot.resttestclient.TestRestTemplate;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import fpt.qn.mes.AbstractIntegrationTest;
import fpt.qn.mes.auth.support.AuthTestData;
import fpt.qn.mes.auth.support.ConcurrentRequestRunner;

@Tag("performance")
@Import(AuthPerformanceIntegrationTest.QueryCountConfiguration.class)
class AuthPerformanceIntegrationTest extends AbstractIntegrationTest {

    static final int CONCURRENCY = 10;
    static final int REQUESTS_PER_WORKER = 10;
    static final int WARM_UP_REQUESTS = 20;

    @Autowired TestRestTemplate restTemplate;
    @Autowired ObjectMapper objectMapper;
    @Autowired AuthTestData authTestData;
    @Autowired AuthorizationQueryCounter queryCounter;

    @Test
    void loginRefreshAndAuthorizationSnapshotMeetReproducibleProfile() throws Exception {
        List<AuthTestData.Identity> identities = new ArrayList<>();
        for (int index = 0; index < CONCURRENCY; index++) {
            identities.add(authTestData.createIdentity(
                    "perf-" + index + "-" + UUID.randomUUID().toString().substring(0, 8),
                    "Password@123",
                    true,
                    List.of(),
                    List.of()));
        }

        try {
            warmUpLogin(identities.getFirst());
            warmUpRefresh(identities.getFirst());

            List<Long> loginDurations = Collections.synchronizedList(new ArrayList<>());
            List<Callable<Integer>> loginWorkers = new ArrayList<>();
            for (AuthTestData.Identity identity : identities) {
                loginWorkers.add(() -> runMeasuredLogins(identity, loginDurations));
            }
            List<Integer> loginStatuses = new ConcurrentRequestRunner().run(
                    loginWorkers, Duration.ofMinutes(2));
            assertThat(loginStatuses).containsOnly(200);

            List<AtomicReference<String>> refreshTokens = new ArrayList<>();
            for (AuthTestData.Identity identity : identities) {
                refreshTokens.add(new AtomicReference<>(
                        login(identity).path("refreshToken").asText()));
            }
            List<Long> refreshDurations = Collections.synchronizedList(new ArrayList<>());
            List<Callable<Integer>> refreshWorkers = new ArrayList<>();
            for (AtomicReference<String> refreshToken : refreshTokens) {
                refreshWorkers.add(() -> runMeasuredRefreshes(refreshToken, refreshDurations));
            }
            List<Integer> refreshStatuses = new ConcurrentRequestRunner().run(
                    refreshWorkers, Duration.ofMinutes(2));
            assertThat(refreshStatuses).containsOnly(200);

            assertThat(loginDurations).hasSize(CONCURRENCY * REQUESTS_PER_WORKER);
            assertThat(refreshDurations).hasSize(CONCURRENCY * REQUESTS_PER_WORKER);
            assertThat(p95Millis(loginDurations)).isLessThan(2_000L);
            assertThat(p95Millis(refreshDurations)).isLessThan(2_000L);

            JsonNode adminTokens = login("admin", "Admin@1234");
            queryCounter.reset();
            ResponseEntity<String> protectedResponse = authorized(
                    "/api/users", adminTokens.path("accessToken").asText());
            assertThat(protectedResponse.getStatusCode().value()).isEqualTo(200);
            assertThat(queryCounter.getAuthorizationSnapshotQueries()).isEqualTo(1);

            System.out.println(
                    "AUTH_PERFORMANCE java=" + System.getProperty("java.version")
                            + " os=" + System.getProperty("os.name")
                            + " postgres=18-alpine concurrency=" + CONCURRENCY
                            + " warmupsPerFlow=" + WARM_UP_REQUESTS
                            + " measuredPerFlow=" + (CONCURRENCY * REQUESTS_PER_WORKER)
                            + " loginP95Ms=" + p95Millis(loginDurations)
                            + " refreshP95Ms=" + p95Millis(refreshDurations)
                            + " authorizationSnapshotQueries="
                            + queryCounter.getAuthorizationSnapshotQueries());
        } finally {
            for (AuthTestData.Identity identity : identities) {
                authTestData.deleteIdentity(identity);
            }
        }
    }

    private void warmUpLogin(AuthTestData.Identity identity) throws Exception {
        for (int index = 0; index < WARM_UP_REQUESTS; index++) {
            login(identity);
        }
    }

    private void warmUpRefresh(AuthTestData.Identity identity) throws Exception {
        String refreshToken = login(identity).path("refreshToken").asText();
        for (int index = 0; index < WARM_UP_REQUESTS; index++) {
            JsonNode refreshed = refresh(refreshToken);
            refreshToken = refreshed.path("refreshToken").asText();
        }
    }

    private int runMeasuredLogins(
            AuthTestData.Identity identity,
            List<Long> durations) throws Exception {
        for (int index = 0; index < REQUESTS_PER_WORKER; index++) {
            long started = System.nanoTime();
            ResponseEntity<String> response = loginResponse(
                    identity.getUsername(), identity.getRawPassword());
            durations.add(Duration.ofNanos(System.nanoTime() - started).toMillis());
            if (response.getStatusCode().value() != 200) {
                return response.getStatusCode().value();
            }
        }
        return 200;
    }

    private int runMeasuredRefreshes(
            AtomicReference<String> refreshToken,
            List<Long> durations) throws Exception {
        for (int index = 0; index < REQUESTS_PER_WORKER; index++) {
            long started = System.nanoTime();
            ResponseEntity<String> response = restTemplate.postForEntity(
                    "/api/auth/refresh",
                    Map.of("refreshToken", refreshToken.get()),
                    String.class);
            durations.add(Duration.ofNanos(System.nanoTime() - started).toMillis());
            if (response.getStatusCode().value() != 200) {
                return response.getStatusCode().value();
            }
            refreshToken.set(objectMapper.readTree(response.getBody())
                    .path("data").path("refreshToken").asText());
        }
        return 200;
    }

    private JsonNode login(AuthTestData.Identity identity) throws Exception {
        return login(identity.getUsername(), identity.getRawPassword());
    }

    private JsonNode login(String username, String password) throws Exception {
        ResponseEntity<String> response = loginResponse(username, password);
        assertThat(response.getStatusCode().value()).isEqualTo(200);
        return objectMapper.readTree(response.getBody()).path("data");
    }

    private ResponseEntity<String> loginResponse(String username, String password) {
        return restTemplate.postForEntity(
                "/api/auth/login",
                Map.of("username", username, "password", password),
                String.class);
    }

    private JsonNode refresh(String refreshToken) throws Exception {
        ResponseEntity<String> response = restTemplate.postForEntity(
                "/api/auth/refresh",
                Map.of("refreshToken", refreshToken),
                String.class);
        assertThat(response.getStatusCode().value()).isEqualTo(200);
        return objectMapper.readTree(response.getBody()).path("data");
    }

    private ResponseEntity<String> authorized(String path, String accessToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        return restTemplate.exchange(
                path, HttpMethod.GET, new HttpEntity<>(null, headers), String.class);
    }

    private long p95Millis(List<Long> durations) {
        List<Long> sorted = new ArrayList<>(durations);
        sorted.sort((left, right) -> left.compareTo(right));
        int index = Math.max(0, (int) Math.ceil(sorted.size() * 0.95) - 1);
        return sorted.get(index);
    }

    static class AuthorizationQueryCounter extends DefaultExecuteListener {

        AtomicInteger authorizationSnapshotQueries = new AtomicInteger();

        @Override
        public void executeStart(ExecuteContext context) {
            String sql = context.sql();
            if (sql != null
                    && sql.contains("\"users\"")
                    && sql.contains("\"user_roles\"")
                    && sql.contains("\"roles\"")
                    && !sql.contains("\"role_permissions\"")
                    && !sql.contains("\"refresh_sessions\"")) {
                authorizationSnapshotQueries.incrementAndGet();
            }
        }

        void reset() {
            authorizationSnapshotQueries.set(0);
        }

        int getAuthorizationSnapshotQueries() {
            return authorizationSnapshotQueries.get();
        }
    }

    @TestConfiguration
    static class QueryCountConfiguration {

        @Bean
        AuthorizationQueryCounter authorizationQueryCounter() {
            return new AuthorizationQueryCounter();
        }

        @Bean
        DefaultConfigurationCustomizer authorizationQueryCountCustomizer(
                AuthorizationQueryCounter counter) {
            return configuration -> configuration.setExecuteListenerProvider(
                    new DefaultExecuteListenerProvider(counter));
        }
    }
}
