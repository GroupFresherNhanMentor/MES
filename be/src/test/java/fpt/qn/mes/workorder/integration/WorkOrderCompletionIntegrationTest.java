package fpt.qn.mes.workorder.integration;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

import fpt.qn.mes.AbstractIntegrationTest;

class WorkOrderCompletionIntegrationTest extends AbstractIntegrationTest {

    @LocalServerPort
    int port;

    @Test
    void complete_returns401WithoutAuthentication() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Content-Type", "application/json");
        String url = "http://localhost:" + port + "/api/v1/work-orders/" + UUID.randomUUID() + "/complete";

        assertThatThrownBy(() -> new RestTemplate().exchange(url, HttpMethod.POST,
                new HttpEntity<>("{}", headers), String.class))
                .isInstanceOf(HttpStatusCodeException.class)
                .satisfies(error -> assertThat(((HttpStatusCodeException) error).getStatusCode())
                        .isEqualTo(HttpStatus.UNAUTHORIZED));
    }
}
