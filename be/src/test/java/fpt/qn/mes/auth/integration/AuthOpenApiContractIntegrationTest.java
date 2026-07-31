package fpt.qn.mes.auth.integration;

import static org.assertj.core.api.Assertions.assertThat;

import java.net.URI;
import java.util.Iterator;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.TestRestTemplate;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import fpt.qn.mes.AbstractIntegrationTest;

class AuthOpenApiContractIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    TestRestTemplate restTemplate;

    @Autowired
    ObjectMapper objectMapper;

    @Test
    void openApiPublishesBearerSecurityAndAllNinetyNineApiOperations() throws Exception {
        var response = restTemplate.getForEntity("/v3/api-docs", String.class);
        assertThat(response.getStatusCode().value()).isEqualTo(200);
        JsonNode document = objectMapper.readTree(response.getBody());
        JsonNode bearer = document.path("components").path("securitySchemes").path("bearerAuth");
        assertThat(bearer.path("type").asText()).isEqualTo("http");
        assertThat(bearer.path("scheme").asText()).isEqualTo("bearer");

        int operationCount = 0;
        Iterator<JsonNode> paths = document.path("paths").elements();
        while (paths.hasNext()) {
            JsonNode path = paths.next();
            for (String method : java.util.List.of("get", "post", "put", "patch", "delete")) {
                if (path.has(method)) {
                    operationCount++;
                    if (!path.get(method).path("operationId").asText().equals("login")
                            && !path.get(method).path("operationId").asText().equals("refresh")
                            && !path.get(method).path("operationId").asText().equals("logout")) {
                        assertThat(path.get(method).path("security").isArray()).isTrue();
                    }
                }
            }
        }
        assertThat(operationCount).isEqualTo(119);


        var groupedResponse = restTemplate.getForEntity(
                URI.create("/v3/api-docs/00.%20All%20Endpoints"),
                String.class);
        assertThat(groupedResponse.getStatusCode().value()).isEqualTo(200);
        JsonNode groupedDocument = objectMapper.readTree(groupedResponse.getBody());
        assertThat(groupedDocument.path("paths")
                        .path("/api/users")
                        .path("get")
                        .path("security")
                        .get(0)
                        .has("bearerAuth"))
                .isTrue();
        assertThat(groupedDocument.path("paths")
                        .path("/api/roles")
                        .path("get")
                        .path("security")
                        .get(0)
                        .has("bearerAuth"))
                .isTrue();
    }
}
