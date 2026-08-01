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
import fpt.qn.mes.master.product.application.dto.producttype.create.CreateProductTypeRequest;

class ProductTypeIntegrationTest extends AbstractIntegrationTest {

    @LocalServerPort
    int port;

    RestTemplate restTemplate;
    HttpHeaders adminHeaders;

    String baseUrl() {
        return "http://localhost:" + port + "/api/product-types";
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
    void getProductTypes_returns401_whenUnauthenticated() {
        assertThatThrownBy(() -> restTemplate.getForEntity(baseUrl(), String.class))
            .isInstanceOf(HttpStatusCodeException.class)
            .satisfies(e -> assertThat(((HttpStatusCodeException) e).getStatusCode())
                .isEqualTo(HttpStatus.UNAUTHORIZED));
    }

    @Test
    void getProductTypes_returns200_whenAuthenticated() {
        var response = restTemplate.exchange(
            baseUrl(), HttpMethod.GET,
            new HttpEntity<>(adminHeaders), String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void getProductTypes_returnsPaginatedResult_whenAuthenticated() {
        var response = restTemplate.exchange(
            baseUrl() + "?page=0&size=10", HttpMethod.GET,
            new HttpEntity<>(adminHeaders), String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).contains("\"success\":true");
    }

    @Test
    void getProductTypes_filtersByName_whenNameParamProvided() {
        var response = restTemplate.exchange(
            baseUrl() + "?name=RAW", HttpMethod.GET,
            new HttpEntity<>(adminHeaders), String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void createProductType_returns401_whenUnauthenticated() {
        CreateProductTypeRequest request = new CreateProductTypeRequest();
        request.setName("TEST_TYPE");
        assertThatThrownBy(() -> restTemplate.postForEntity(baseUrl(), request, String.class))
            .isInstanceOf(HttpStatusCodeException.class)
            .satisfies(e -> assertThat(((HttpStatusCodeException) e).getStatusCode())
                .isEqualTo(HttpStatus.UNAUTHORIZED));
    }

    @Test
    void createProductType_returns400_whenNameIsBlank() {
        CreateProductTypeRequest request = new CreateProductTypeRequest();
        request.setName("");
        assertThatThrownBy(() -> restTemplate.exchange(
            baseUrl(), HttpMethod.POST,
            new HttpEntity<>(request, adminHeaders), String.class))
            .isInstanceOf(HttpStatusCodeException.class)
            .satisfies(e -> assertThat(((HttpStatusCodeException) e).getStatusCode())
                .isEqualTo(HttpStatus.BAD_REQUEST));
    }

    @Test
    void createProductType_returns201_whenValid() {
        CreateProductTypeRequest request = new CreateProductTypeRequest();
        request.setName("IT_TYPE_" + System.currentTimeMillis());
        request.setDescription("Created in integration test");
        var response = restTemplate.exchange(
            baseUrl(), HttpMethod.POST,
            new HttpEntity<>(request, adminHeaders), String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    }

    @Test
    void createProductType_returns409_whenNameAlreadyExists() {
        String name = "IT_TYPE_DUP_" + System.currentTimeMillis();
        CreateProductTypeRequest request = new CreateProductTypeRequest();
        request.setName(name);
        restTemplate.exchange(baseUrl(), HttpMethod.POST, new HttpEntity<>(request, adminHeaders), String.class);

        assertThatThrownBy(() -> restTemplate.exchange(
            baseUrl(), HttpMethod.POST, new HttpEntity<>(request, adminHeaders), String.class))
            .isInstanceOf(HttpStatusCodeException.class)
            .satisfies(e -> assertThat(((HttpStatusCodeException) e).getStatusCode()).isEqualTo(HttpStatus.CONFLICT));
    }
}
