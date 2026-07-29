package fpt.qn.mes.auth.integration;

import static org.assertj.core.api.Assertions.assertThat;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.jooq.DSLContext;
import org.jooq.impl.DSL;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;

import fpt.qn.mes.AbstractIntegrationTest;
import fpt.qn.mes.auth.support.AuthTestData;
import fpt.qn.mes.auth.support.EndpointPermissionCatalog;

class EndpointAuthorizationIntegrationTest extends AbstractIntegrationTest {

    @Autowired TestRestTemplate restTemplate;
    @Autowired ObjectMapper objectMapper;
    @Autowired AuthTestData authTestData;
    @Autowired DSLContext ctx;

    @Test
    void everyProtectedCatalogEntryRejectsUnauthenticatedAndMissingAuthorityRequests()
            throws Exception {
        String suffix = UUID.randomUUID().toString().substring(0, 8);
        var limited = authTestData.createIdentity(
                "matrix-" + suffix,
                "Password@123",
                true,
                List.of("MATRIX_" + suffix.toUpperCase()),
                List.of());
        try {
            String limitedToken = login(
                    limited.getUsername(), limited.getRawPassword());
            String adminToken = login("admin", "Admin@1234");
            JsonNode openApi = objectMapper.readTree(
                    restTemplate.getForEntity("/v3/api-docs", String.class).getBody());

            int protectedEntries = 0;
            for (EndpointPermissionCatalog.EndpointSpec spec
                    : EndpointPermissionCatalog.specifications()) {
                if (EndpointPermissionCatalog.PUBLIC.equals(spec.getAuthority())) {
                    continue;
                }
                protectedEntries++;
                RequestFixture validFixture = fixture(openApi, spec, true);

                Map<String, Long> before = spec.hasMutationProbe()
                        ? databaseCounts() : Map.of();
                ResponseEntity<String> unauthenticated = exchange(
                        spec, validFixture, null);
                assertThat(unauthenticated.getStatusCode().value())
                        .as(spec.key() + " unauthenticated")
                        .isEqualTo(401);

                ResponseEntity<String> forbidden = exchange(
                        spec, validFixture, limitedToken);
                assertThat(forbidden.getStatusCode().value())
                        .as(spec.key() + " missing authority")
                        .isEqualTo(403);
                if (spec.hasMutationProbe()) {
                    assertThat(databaseCounts())
                            .as(spec.key() + " denied mutation probe")
                            .isEqualTo(before);
                }

                RequestFixture safeAllowedFixture = fixture(openApi, spec, false);
                ResponseEntity<String> allowed = exchange(
                        spec, safeAllowedFixture, adminToken);
                assertThat(spec.getAllowedStatuses())
                        .as(spec.key() + " allowed authority")
                        .contains(allowed.getStatusCode().value());
            }
            assertThat(protectedEntries).isEqualTo(89);
        } finally {
            authTestData.deleteIdentity(limited);
        }
    }

    private RequestFixture fixture(
            JsonNode openApi,
            EndpointPermissionCatalog.EndpointSpec spec,
            boolean validBody) {
        JsonNode operation = openApi.path("paths")
                .path(spec.getPath())
                .path(spec.getMethod().toLowerCase(java.util.Locale.ROOT));
        String resolvedPath = spec.getPath();
        List<String> query = new ArrayList<>();
        for (JsonNode parameter : operation.path("parameters")) {
            String name = parameter.path("name").asText();
            String location = parameter.path("in").asText();
            JsonNode value = scalar(
                    dereference(openApi, parameter.path("schema")),
                    name);
            if ("path".equals(location)) {
                resolvedPath = resolvedPath.replace(
                        "{" + name + "}",
                        URLEncoder.encode(value.asText(), StandardCharsets.UTF_8));
            } else if ("query".equals(location) && parameter.path("required").asBoolean()) {
                query.add(URLEncoder.encode(name, StandardCharsets.UTF_8)
                        + "="
                        + URLEncoder.encode(value.asText(), StandardCharsets.UTF_8));
            }
        }
        resolvedPath = resolvedPath.replaceAll(
                "\\{[^/]+\\}", UUID.randomUUID().toString());
        if (!query.isEmpty()) {
            resolvedPath = resolvedPath + "?" + String.join("&", query);
        }

        JsonNode body = null;
        JsonNode schema = operation.path("requestBody")
                .path("content")
                .path(MediaType.APPLICATION_JSON_VALUE)
                .path("schema");
        if (!schema.isMissingNode()) {
            body = validBody
                    ? example(openApi, schema, "request")
                    : JsonNodeFactory.instance.objectNode();
        }
        return new RequestFixture(resolvedPath, body);
    }

    private JsonNode example(JsonNode openApi, JsonNode rawSchema, String propertyName) {
        JsonNode schema = dereference(openApi, rawSchema);
        if (schema.has("oneOf") && !schema.path("oneOf").isEmpty()) {
            return example(openApi, schema.path("oneOf").get(0), propertyName);
        }
        if (schema.has("anyOf") && !schema.path("anyOf").isEmpty()) {
            return example(openApi, schema.path("anyOf").get(0), propertyName);
        }
        if (schema.has("properties") || "object".equals(schema.path("type").asText())) {
            ObjectNode result = JsonNodeFactory.instance.objectNode();
            Set<String> required = new LinkedHashSet<>();
            for (JsonNode requiredName : schema.path("required")) {
                required.add(requiredName.asText());
            }
            Iterator<Map.Entry<String, JsonNode>> properties =
                    schema.path("properties").fields();
            while (properties.hasNext()) {
                Map.Entry<String, JsonNode> property = properties.next();
                if (required.contains(property.getKey())) {
                    result.set(
                            property.getKey(),
                            example(openApi, property.getValue(), property.getKey()));
                }
            }
            return result;
        }
        if ("array".equals(schema.path("type").asText())) {
            ArrayNode result = JsonNodeFactory.instance.arrayNode();
            if (schema.path("minItems").asInt(0) > 0) {
                result.add(example(openApi, schema.path("items"), propertyName));
            }
            return result;
        }
        return scalar(schema, propertyName);
    }

    private JsonNode scalar(JsonNode schema, String propertyName) {
        if (schema.has("enum") && !schema.path("enum").isEmpty()) {
            return schema.path("enum").get(0);
        }
        String type = schema.path("type").asText();
        String format = schema.path("format").asText();
        String normalizedName = propertyName.toLowerCase(java.util.Locale.ROOT);
        if ("boolean".equals(type)) {
            return JsonNodeFactory.instance.booleanNode(true);
        }
        if ("integer".equals(type)) {
            return JsonNodeFactory.instance.numberNode(
                    Math.max(1, schema.path("minimum").asInt(1)));
        }
        if ("number".equals(type)) {
            return JsonNodeFactory.instance.numberNode(1);
        }
        if ("uuid".equals(format) || normalizedName.endsWith("id")) {
            return JsonNodeFactory.instance.textNode(UUID.randomUUID().toString());
        }
        if ("date".equals(format)) {
            return JsonNodeFactory.instance.textNode(LocalDate.now().toString());
        }
        if ("date-time".equals(format)) {
            return JsonNodeFactory.instance.textNode(Instant.now().toString());
        }
        if (normalizedName.contains("password")) {
            return JsonNodeFactory.instance.textNode("Password@123");
        }
        if (normalizedName.contains("username")) {
            return JsonNodeFactory.instance.textNode(
                    "matrix-" + UUID.randomUUID().toString().substring(0, 8));
        }
        if ("name".equals(normalizedName)) {
            return JsonNodeFactory.instance.textNode(
                    "MATRIX_ACTION_" + UUID.randomUUID().toString()
                            .substring(0, 8).toUpperCase(java.util.Locale.ROOT));
        }
        int minimumLength = Math.max(1, schema.path("minLength").asInt(1));
        String value = "MATRIX";
        while (value.length() < minimumLength) {
            value = value + "X";
        }
        return JsonNodeFactory.instance.textNode(value);
    }

    private JsonNode dereference(JsonNode openApi, JsonNode schema) {
        String reference = schema.path("$ref").asText();
        if (reference.isBlank()) {
            return schema;
        }
        JsonNode current = openApi;
        for (String segment : reference.substring(2).split("/")) {
            current = current.path(segment);
        }
        return current;
    }

    private ResponseEntity<String> exchange(
            EndpointPermissionCatalog.EndpointSpec spec,
            RequestFixture fixture,
            String token) {
        HttpHeaders headers = new HttpHeaders();
        if (token != null) {
            headers.setBearerAuth(token);
        }
        Object body = null;
        if (fixture.body != null) {
            headers.setContentType(MediaType.APPLICATION_JSON);
            body = fixture.body.toString();
        }
        return restTemplate.exchange(
                fixture.path,
                HttpMethod.valueOf(spec.getMethod()),
                new HttpEntity<>(body, headers),
                String.class);
    }

    private Map<String, Long> databaseCounts() {
        List<String> tables = ctx.select(DSL.field("table_name", String.class))
                .from("information_schema.tables")
                .where(DSL.field("table_schema").eq("public"))
                .and(DSL.field("table_type").eq("BASE TABLE"))
                .orderBy(DSL.field("table_name"))
                .fetch(0, String.class);
        Map<String, Long> result = new LinkedHashMap<>();
        for (String table : tables) {
            result.put(
                    table,
                    ctx.selectCount()
                            .from(DSL.table(DSL.name("public", table)))
                            .fetchOne(0, long.class));
        }
        return result;
    }

    private String login(String username, String password) throws Exception {
        ResponseEntity<String> response = restTemplate.postForEntity(
                "/api/auth/login",
                Map.of("username", username, "password", password),
                String.class);
        assertThat(response.getStatusCode().value()).isEqualTo(200);
        return objectMapper.readTree(response.getBody())
                .path("data").path("accessToken").asText();
    }

    private static final class RequestFixture {

        final String path;
        final JsonNode body;

        RequestFixture(String path, JsonNode body) {
            this.path = path;
            this.body = body;
        }
    }
}
