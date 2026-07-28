package fpt.qn.mes.quality.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

import fpt.qn.mes.AbstractIntegrationTest;
import fpt.qn.mes.quality.application.dto.request.FailQcRequest;
import fpt.qn.mes.quality.application.dto.request.PassQcRequest;

class QualityInspectionIntegrationTest extends AbstractIntegrationTest {

    @LocalServerPort
    int port;

    RestTemplate restTemplate;

    HttpHeaders adminHeaders;
    UUID inspectionId;

    String baseUrl() {
        return "http://localhost:" + port + "/api/quality-inspections";
    }

    @BeforeEach
    void setUp() {
        restTemplate = new RestTemplate();

        seedAdminUser();
        adminHeaders = new HttpHeaders();
        adminHeaders.setBearerAuth(generateToken("admin", "ADMIN"));
        adminHeaders.set("Content-Type", "application/json");
        inspectionId = UUID.randomUUID();
    }

    @Test
    void getInspections_returns401_whenUnauthenticated() {
        assertThatThrownBy(() -> restTemplate.getForEntity(baseUrl(), String.class))
            .isInstanceOf(HttpStatusCodeException.class)
            .satisfies(e -> assertThat(((HttpStatusCodeException) e).getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED));
    }

    @Test
    void getInspections_returns200_whenAuthenticated() {
        var response = restTemplate.exchange(
            baseUrl(), HttpMethod.GET,
            new HttpEntity<>(adminHeaders),
            String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void getInspectionById_returns404_whenNotFound() {
        assertThatThrownBy(() -> restTemplate.exchange(
            baseUrl() + "/" + UUID.randomUUID(), HttpMethod.GET,
            new HttpEntity<>(adminHeaders),
            String.class))
            .isInstanceOf(HttpStatusCodeException.class)
            .satisfies(e -> assertThat(((HttpStatusCodeException) e).getStatusCode().value()).isEqualTo(404));
    }

    @Test
    void passQc_returns404_whenInspectionNotFound() {
        PassQcRequest request = new PassQcRequest();
        request.setPassedQuantity(BigDecimal.valueOf(99999));

        assertThatThrownBy(() -> restTemplate.exchange(
            baseUrl() + "/" + inspectionId + "/pass", HttpMethod.POST,
            new HttpEntity<>(request, adminHeaders),
            String.class))
            .isInstanceOf(HttpStatusCodeException.class)
            .satisfies(e -> assertThat(((HttpStatusCodeException) e).getStatusCode().value()).isEqualTo(404));
    }

    @Test
    void failQc_returns400_whenMissingDefectType() {
        FailQcRequest request = new FailQcRequest();
        request.setFailedQuantity(BigDecimal.TEN);
        request.setActionId(UUID.randomUUID());
        request.setDefectTypeId(null);
        request.setReason("Defect reason");

        assertThatThrownBy(() -> restTemplate.exchange(
            baseUrl() + "/" + inspectionId + "/fail", HttpMethod.POST,
            new HttpEntity<>(request, adminHeaders),
            String.class))
            .isInstanceOf(HttpStatusCodeException.class)
            .satisfies(e -> assertThat(((HttpStatusCodeException) e).getStatusCode().value()).isEqualTo(400));
    }

    @Test
    void failQc_returns400_whenMissingReason() {
        FailQcRequest request = new FailQcRequest();
        request.setFailedQuantity(BigDecimal.TEN);
        request.setActionId(UUID.randomUUID());
        request.setDefectTypeId(UUID.randomUUID());
        request.setReason(null);

        assertThatThrownBy(() -> restTemplate.exchange(
            baseUrl() + "/" + inspectionId + "/fail", HttpMethod.POST,
            new HttpEntity<>(request, adminHeaders),
            String.class))
            .isInstanceOf(HttpStatusCodeException.class)
            .satisfies(e -> assertThat(((HttpStatusCodeException) e).getStatusCode().value()).isEqualTo(400));
    }

    @Test
    void getQcStatuses_returns200() {
        ResponseEntity<String> response = restTemplate.exchange(
            baseUrl() + "/statuses", HttpMethod.GET,
            new HttpEntity<>(adminHeaders),
            String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void getQcActions_returns200() {
        ResponseEntity<String> response = restTemplate.exchange(
            baseUrl() + "/actions", HttpMethod.GET,
            new HttpEntity<>(adminHeaders),
            String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void getDefectTypes_returns200() {
        ResponseEntity<String> response = restTemplate.exchange(
            baseUrl() + "/defect-types", HttpMethod.GET,
            new HttpEntity<>(adminHeaders),
            String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void createQcStatus_returns401_whenUnauthenticated() {
        String body = "{\"name\":\"NEW_STATUS\"}";
        HttpHeaders headers = new HttpHeaders();
        headers.set("Content-Type", "application/json");

        assertThatThrownBy(() -> restTemplate.exchange(
            baseUrl() + "/statuses", HttpMethod.POST,
            new HttpEntity<>(body, headers),
            String.class))
            .isInstanceOf(HttpStatusCodeException.class)
            .satisfies(e -> assertThat(((HttpStatusCodeException) e).getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED));
    }
}
