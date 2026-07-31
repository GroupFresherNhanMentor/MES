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
import fpt.qn.mes.master.product.application.dto.productstatus.create.CreateProductStatusRequest;

class ProductStatusIntegrationTest extends AbstractIntegrationTest {

    @LocalServerPort
    int port;

    RestTemplate restTemplate;
    HttpHeaders adminHeaders;

    String baseUrl() {
        return "http://localhost:" + port + "/api/product-statuses";
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
    void getProductStatuses_returns401_whenUnauthenticated() {
        assertThatThrownBy(() -> restTemplate.getForEntity(baseUrl(), String.class))
            .isInstanceOf(HttpStatusCodeException.class)
            .satisfies(e -> assertThat(((HttpStatusCodeException) e).getStatusCode())
                .isEqualTo(HttpStatus.UNAUTHORIZED));
    }

    @Test
    void getProductStatuses_returns200_whenAuthenticated() {
        var response = restTemplate.exchange(
            baseUrl(), HttpMethod.GET,
            new HttpEntity<>(adminHeaders), String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void getProductStatuses_returnsPaginatedResult_whenAuthenticated() {
        var response = restTemplate.exchange(
            baseUrl() + "?page=0&size=10", HttpMethod.GET,
            new HttpEntity<>(adminHeaders), String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).contains("\"success\":true");
    }

    @Test
    void getProductStatuses_filtersbyName_whenNameParamProvided() {
        var response = restTemplate.exchange(
            baseUrl() + "?name=ACTIVE", HttpMethod.GET,
            new HttpEntity<>(adminHeaders), String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void createProductStatus_returns401_whenUnauthenticated() {
        CreateProductStatusRequest request = new CreateProductStatusRequest();
        request.setName("TEST_STATUS");
        assertThatThrownBy(() -> restTemplate.postForEntity(baseUrl(), request, String.class))
            .isInstanceOf(HttpStatusCodeException.class)
            .satisfies(e -> assertThat(((HttpStatusCodeException) e).getStatusCode())
                .isEqualTo(HttpStatus.UNAUTHORIZED));
    }

    @Test
    void createProductStatus_returns400_whenNameIsBlank() {
        CreateProductStatusRequest request = new CreateProductStatusRequest();
        request.setName("");
        assertThatThrownBy(() -> restTemplate.exchange(
            baseUrl(), HttpMethod.POST,
            new HttpEntity<>(request, adminHeaders), String.class))
            .isInstanceOf(HttpStatusCodeException.class)
            .satisfies(e -> assertThat(((HttpStatusCodeException) e).getStatusCode())
                .isEqualTo(HttpStatus.BAD_REQUEST));
    }

    @Test
    void createProductStatus_returns201_whenValid() {
        CreateProductStatusRequest request = new CreateProductStatusRequest();
        request.setName("IT_STATUS_" + System.currentTimeMillis());
        request.setDescription("Created in integration test");
        var response = restTemplate.exchange(
            baseUrl(), HttpMethod.POST,
            new HttpEntity<>(request, adminHeaders), String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    }

    @Test
    void createProductStatus_returns409_whenNameAlreadyExists() {
        String name = "IT_STATUS_DUP_" + System.currentTimeMillis();
        CreateProductStatusRequest request = new CreateProductStatusRequest();
        request.setName(name);
        restTemplate.exchange(baseUrl(), HttpMethod.POST, new HttpEntity<>(request, adminHeaders), String.class);

        assertThatThrownBy(() -> restTemplate.exchange(
            baseUrl(), HttpMethod.POST, new HttpEntity<>(request, adminHeaders), String.class))
            .isInstanceOf(HttpStatusCodeException.class)
            .satisfies(e -> assertThat(((HttpStatusCodeException) e).getStatusCode()).isEqualTo(HttpStatus.CONFLICT));
    }
}
