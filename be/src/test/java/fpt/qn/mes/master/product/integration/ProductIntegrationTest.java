package fpt.qn.mes.master.product.integration;

import static fpt.qn.mes.jooq.Tables.PRODUCT_STATUSES;
import static fpt.qn.mes.jooq.Tables.PRODUCT_TYPES;
import static fpt.qn.mes.jooq.Tables.UNITS_OF_MEASURE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.UUID;

import org.jooq.DSLContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

import fpt.qn.mes.AbstractIntegrationTest;
import fpt.qn.mes.master.product.application.dto.product.create.CreateProductRequest;
import fpt.qn.mes.master.product.application.dto.product.update.UpdateProductRequest;

class ProductIntegrationTest extends AbstractIntegrationTest {

    @LocalServerPort int port;
    @Autowired DSLContext dslCtx;

    RestTemplate restTemplate;
    HttpHeaders adminHeaders;
    UUID productTypeId;
    UUID unitId;
    UUID statusId;

    String baseUrl() {
        return "http://localhost:" + port + "/api/products";
    }

    @BeforeEach
    void setUp() {
        restTemplate = new RestTemplate();
        seedAdminUser();
        adminHeaders = new HttpHeaders();
        adminHeaders.setBearerAuth(generateToken("admin", "ADMIN"));
        adminHeaders.set("Content-Type", "application/json");

        productTypeId = dslCtx.select(PRODUCT_TYPES.ID).from(PRODUCT_TYPES).limit(1).fetchOne(PRODUCT_TYPES.ID);
        unitId = dslCtx.select(UNITS_OF_MEASURE.ID).from(UNITS_OF_MEASURE).limit(1).fetchOne(UNITS_OF_MEASURE.ID);
        statusId = dslCtx.select(PRODUCT_STATUSES.ID).from(PRODUCT_STATUSES).limit(1).fetchOne(PRODUCT_STATUSES.ID);
    }

    // ── auth ──────────────────────────────────────────────────────────────────

    @Test
    void getProducts_returns401_whenUnauthenticated() {
        assertThatThrownBy(() -> restTemplate.getForEntity(baseUrl(), String.class))
            .isInstanceOf(HttpStatusCodeException.class)
            .satisfies(e -> assertThat(((HttpStatusCodeException) e).getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED));
    }

    @Test
    void createProduct_returns401_whenUnauthenticated() {
        assertThatThrownBy(() -> restTemplate.postForEntity(baseUrl(), validRequest("AUTH-001"), String.class))
            .isInstanceOf(HttpStatusCodeException.class)
            .satisfies(e -> assertThat(((HttpStatusCodeException) e).getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED));
    }

    // ── list ──────────────────────────────────────────────────────────────────

    @Test
    void getProducts_returns200_whenAuthenticated() {
        var response = restTemplate.exchange(baseUrl(), HttpMethod.GET, new HttpEntity<>(adminHeaders), String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).contains("\"success\":true");
    }

    @Test
    void getProducts_filtersByCode_whenCodeParamProvided() {
        var response = restTemplate.exchange(
            baseUrl() + "?code=NOTEXIST", HttpMethod.GET, new HttpEntity<>(adminHeaders), String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    // ── create ────────────────────────────────────────────────────────────────

    @Test
    void createProduct_returns201_whenValid() {
        var response = restTemplate.exchange(
            baseUrl(), HttpMethod.POST, new HttpEntity<>(validRequest("IT-CREATE-001"), adminHeaders), String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    }

    @Test
    void createProduct_returns400_whenCodeIsBlank() {
        var req = validRequest("");
        assertThatThrownBy(() -> restTemplate.exchange(
            baseUrl(), HttpMethod.POST, new HttpEntity<>(req, adminHeaders), String.class))
            .isInstanceOf(HttpStatusCodeException.class)
            .satisfies(e -> assertThat(((HttpStatusCodeException) e).getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST));
    }

    @Test
    void createProduct_returns400_whenVersionIsBlank() {
        var req = validRequest("IT-VER-001");
        req.setVersion("");
        assertThatThrownBy(() -> restTemplate.exchange(
            baseUrl(), HttpMethod.POST, new HttpEntity<>(req, adminHeaders), String.class))
            .isInstanceOf(HttpStatusCodeException.class)
            .satisfies(e -> assertThat(((HttpStatusCodeException) e).getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST));
    }

    @Test
    void createProduct_returns409_whenCodeAlreadyExists() {
        restTemplate.exchange(baseUrl(), HttpMethod.POST, new HttpEntity<>(validRequest("IT-DUP-001"), adminHeaders), String.class);

        assertThatThrownBy(() -> restTemplate.exchange(
            baseUrl(), HttpMethod.POST, new HttpEntity<>(validRequest("IT-DUP-001"), adminHeaders), String.class))
            .isInstanceOf(HttpStatusCodeException.class)
            .satisfies(e -> assertThat(((HttpStatusCodeException) e).getStatusCode()).isEqualTo(HttpStatus.CONFLICT));
    }

    // ── get by id ─────────────────────────────────────────────────────────────

    @Test
    void getProductById_returns404_whenDoesNotExist() {
        assertThatThrownBy(() -> restTemplate.exchange(
            baseUrl() + "/" + UUID.randomUUID(), HttpMethod.GET, new HttpEntity<>(adminHeaders), String.class))
            .isInstanceOf(HttpStatusCodeException.class)
            .satisfies(e -> assertThat(((HttpStatusCodeException) e).getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND));
    }

    @Test
    void getProductById_returns200_whenExists() {
        restTemplate.exchange(baseUrl(), HttpMethod.POST, new HttpEntity<>(validRequest("IT-GET-001"), adminHeaders), String.class);

        var list = restTemplate.exchange(baseUrl() + "?code=IT-GET-001", HttpMethod.GET, new HttpEntity<>(adminHeaders), String.class);
        assertThat(list.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    // ── update ────────────────────────────────────────────────────────────────

    @Test
    void updateProduct_returns404_whenDoesNotExist() {
        var req = new UpdateProductRequest();
        req.setName("Updated");
        assertThatThrownBy(() -> restTemplate.exchange(
            baseUrl() + "/" + UUID.randomUUID(), HttpMethod.PUT, new HttpEntity<>(req, adminHeaders), String.class))
            .isInstanceOf(HttpStatusCodeException.class)
            .satisfies(e -> assertThat(((HttpStatusCodeException) e).getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND));
    }

    // ── activate / deactivate ─────────────────────────────────────────────────

    @Test
    void activateProduct_returns404_whenDoesNotExist() {
        assertThatThrownBy(() -> restTemplate.exchange(
            baseUrl() + "/" + UUID.randomUUID() + "/activate", HttpMethod.PUT, new HttpEntity<>(adminHeaders), String.class))
            .isInstanceOf(HttpStatusCodeException.class)
            .satisfies(e -> assertThat(((HttpStatusCodeException) e).getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND));
    }

    @Test
    void deactivateProduct_returns404_whenDoesNotExist() {
        assertThatThrownBy(() -> restTemplate.exchange(
            baseUrl() + "/" + UUID.randomUUID() + "/deactivate", HttpMethod.PUT, new HttpEntity<>(adminHeaders), String.class))
            .isInstanceOf(HttpStatusCodeException.class)
            .satisfies(e -> assertThat(((HttpStatusCodeException) e).getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND));
    }

    // ── helpers ───────────────────────────────────────────────────────────────

    private CreateProductRequest validRequest(String code) {
        var req = new CreateProductRequest();
        req.setCode(code);
        req.setName("Integration Test Product");
        req.setVersion("v1.0-" + code);
        req.setProductTypeId(productTypeId);
        req.setUnitId(unitId);
        return req;
    }
}
