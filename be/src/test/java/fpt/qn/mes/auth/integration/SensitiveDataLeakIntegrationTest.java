package fpt.qn.mes.auth.integration;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.boot.resttestclient.TestRestTemplate;
import org.springframework.http.ResponseEntity;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import fpt.qn.mes.AbstractIntegrationTest;
import fpt.qn.mes.auth.support.AuthTestData;

class SensitiveDataLeakIntegrationTest extends AbstractIntegrationTest {

    @Autowired TestRestTemplate restTemplate;
    @Autowired ObjectMapper objectMapper;
    @Autowired AuthTestData authTestData;

    @Test
    @ExtendWith(OutputCaptureExtension.class)
    void credentialsAndTokensNeverAppearInErrorsOrLogs(CapturedOutput output)
            throws Exception {
        String suffix = UUID.randomUUID().toString().substring(0, 8);
        String password = "Secret@" + suffix;
        var identity = authTestData.createIdentity(
                "sensitive-" + suffix,
                password,
                true,
                List.of("SENSITIVE_" + suffix.toUpperCase()),
                List.of("USER_READ"));
        try {
            ResponseEntity<String> loginResponse = restTemplate.postForEntity(
                    "/api/auth/login",
                    Map.of("username", identity.getUsername(), "password", password),
                    String.class);
            JsonNode tokens = objectMapper.readTree(loginResponse.getBody()).path("data");
            String accessToken = tokens.path("accessToken").asText();
            String refreshToken = tokens.path("refreshToken").asText();

            ResponseEntity<String> failedLogin = restTemplate.postForEntity(
                    "/api/auth/login",
                    Map.of("username", identity.getUsername(), "password", password + "-wrong"),
                    String.class);
            ResponseEntity<String> failedRefresh = restTemplate.postForEntity(
                    "/api/auth/refresh",
                    Map.of("refreshToken", "malformed." + suffix + ".token"),
                    String.class);

            assertThat(failedLogin.getBody())
                    .doesNotContain(password, accessToken, refreshToken, "$2a$", "passwordHash");
            assertThat(failedRefresh.getBody())
                    .doesNotContain(password, accessToken, refreshToken, "$2a$", "passwordHash");

            assertThat(output.getAll())
                    .doesNotContain(password, accessToken, refreshToken, "$2a$", "passwordHash");
        } finally {
            authTestData.deleteIdentity(identity);
        }
    }
}
