package fpt.qn.mes.auth.support;

import org.springframework.boot.resttestclient.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class SecurityTestClient {

    TestRestTemplate restTemplate;

    public ResponseEntity<String> exchange(
            String path,
            HttpMethod method,
            Object request,
            String accessToken) {
        HttpHeaders headers = new HttpHeaders();
        if (accessToken != null && !accessToken.isBlank()) {
            headers.setBearerAuth(accessToken);
        }
        return restTemplate.exchange(
                path,
                method,
                new HttpEntity<>(request, headers),
                String.class);
    }

    public ResponseEntity<String> unauthenticated(
            String path,
            HttpMethod method,
            Object request) {
        return exchange(path, method, request, null);
    }
}
