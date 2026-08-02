package fpt.qn.mes.master.product.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

import fpt.qn.mes.AbstractIntegrationTest;
import fpt.qn.mes.master.product.application.dto.unitofmeasure.create.CreateUnitOfMeasureRequest;

class UnitOfMeasureIntegrationTest extends AbstractIntegrationTest {

    @LocalServerPort
    int port;

    RestTemplate restTemplate;
    HttpHeaders adminHeaders;

    String baseUrl() {
        return "http://localhost:" + port + "/api/units-of-measure";
    }

    @BeforeEach
    void setUp() {
        restTemplate = new RestTemplate();
        seedAdminUser();
        adminHeaders = new HttpHeaders();
        adminHeaders.setBearerAuth(generateToken("admin", "ADMIN"));
        adminHeaders.set("Content-Type", "application/json");
    }

    @Test
    void getUnitsOfMeasure_returns401_whenUnauthenticated() {
        assertThatThrownBy(() -> restTemplate.getForEntity(baseUrl(), String.class))
            .isInstanceOf(HttpStatusCodeException.class)
            .satisfies(e -> assertThat(((HttpStatusCodeException) e).getStatusCode())
                .isEqualTo(HttpStatus.UNAUTHORIZED));
    }

    @Test
    void getUnitsOfMeasure_returns200_whenAuthenticated() {
        var response = restTemplate.exchange(
            baseUrl(), HttpMethod.GET,
            new HttpEntity<>(adminHeaders), String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void getUnitsOfMeasure_returnsPaginatedResult_whenAuthenticated() {
        var response = restTemplate.exchange(
            baseUrl() + "?page=0&size=10", HttpMethod.GET,
            new HttpEntity<>(adminHeaders), String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).contains("\"success\":true");
    }

    @Test
    void getUnitsOfMeasure_filtersByName_whenNameParamProvided() {
        var response = restTemplate.exchange(
            baseUrl() + "?name=KG", HttpMethod.GET,
            new HttpEntity<>(adminHeaders), String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void createUnitOfMeasure_returns401_whenUnauthenticated() {
        CreateUnitOfMeasureRequest request = new CreateUnitOfMeasureRequest();
        request.setName("TEST_UNIT");
        assertThatThrownBy(() -> restTemplate.postForEntity(baseUrl(), request, String.class))
            .isInstanceOf(HttpStatusCodeException.class)
            .satisfies(e -> assertThat(((HttpStatusCodeException) e).getStatusCode())
                .isEqualTo(HttpStatus.UNAUTHORIZED));
    }

    @Test
    void createUnitOfMeasure_returns400_whenNameIsBlank() {
        CreateUnitOfMeasureRequest request = new CreateUnitOfMeasureRequest();
        request.setName("");
        assertThatThrownBy(() -> restTemplate.exchange(
            baseUrl(), HttpMethod.POST,
            new HttpEntity<>(request, adminHeaders), String.class))
            .isInstanceOf(HttpStatusCodeException.class)
            .satisfies(e -> assertThat(((HttpStatusCodeException) e).getStatusCode())
                .isEqualTo(HttpStatus.BAD_REQUEST));
    }

    @Test
    void createUnitOfMeasure_returns201_whenValid() {
        CreateUnitOfMeasureRequest request = new CreateUnitOfMeasureRequest();
        request.setName("IT_UNIT_" + System.currentTimeMillis());
        request.setDescription("Created in integration test");
        var response = restTemplate.exchange(
            baseUrl(), HttpMethod.POST,
            new HttpEntity<>(request, adminHeaders), String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    }

    @Test
    void createUnitOfMeasure_returns409_whenNameAlreadyExists() {
        String name = "IT_UNIT_DUP_" + System.currentTimeMillis();
        CreateUnitOfMeasureRequest request = new CreateUnitOfMeasureRequest();
        request.setName(name);
        restTemplate.exchange(baseUrl(), HttpMethod.POST, new HttpEntity<>(request, adminHeaders), String.class);

        assertThatThrownBy(() -> restTemplate.exchange(
            baseUrl(), HttpMethod.POST, new HttpEntity<>(request, adminHeaders), String.class))
            .isInstanceOf(HttpStatusCodeException.class)
            .satisfies(e -> assertThat(((HttpStatusCodeException) e).getStatusCode()).isEqualTo(HttpStatus.CONFLICT));
    }
}
