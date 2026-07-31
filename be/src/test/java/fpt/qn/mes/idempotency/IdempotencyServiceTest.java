package fpt.qn.mes.idempotency;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import fpt.qn.mes.idempotency.application.exception.IdempotencyConflictException;
import fpt.qn.mes.idempotency.application.exception.IdempotencyPayloadMismatchException;
import fpt.qn.mes.idempotency.application.service.IdempotencyService;
import fpt.qn.mes.idempotency.application.service.IdempotencyService.ClaimResult;
import fpt.qn.mes.idempotency.domain.entities.IdempotencyKey;
import fpt.qn.mes.idempotency.domain.repository.IdempotencyKeyRepository;

@ExtendWith(MockitoExtension.class)
class IdempotencyServiceTest {

    @Mock
    private IdempotencyKeyRepository repository;

    @InjectMocks
    private IdempotencyService idempotencyService;

    private UUID userId;
    private String key;
    private String clientIdentifier;
    private String requestPath;
    private String requestMethod;
    private byte[] bodyBytes;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        key = "test-key-123";
        clientIdentifier = "127.0.0.1:curl/7.68.0";
        requestPath = "/api/stock-in";
        requestMethod = "POST";
        bodyBytes = "{\"productId\":\"123\"}".getBytes();
    }

    @Test
    @DisplayName("claimOrRetrieve should return new claim when key does not exist")
    void claimOrRetrieve_NewKey_Success() {
        when(repository.findByKeyAndUserOrClient(key, userId, clientIdentifier)).thenReturn(Optional.empty());

        IdempotencyKey savedKey = IdempotencyKey.builder()
                .id(UUID.randomUUID())
                .key(key)
                .userId(userId)
                .requestMethod(requestMethod)
                .requestHash(IdempotencyService.computeSha256(bodyBytes))
                .build();
        when(repository.save(any(IdempotencyKey.class))).thenReturn(savedKey);

        ClaimResult result = idempotencyService.claimOrRetrieve(key, userId, clientIdentifier, requestPath, requestMethod, bodyBytes);

        assertThat(result.isNewClaim()).isTrue();
        assertThat(result.record().getKey()).isEqualTo(key);
        verify(repository).save(any(IdempotencyKey.class));
    }

    @Test
    @DisplayName("claimOrRetrieve should return cached response when completed key exists with matching hash")
    void claimOrRetrieve_CompletedKey_ReturnsCached() {
        String hash = IdempotencyService.computeSha256(bodyBytes);
        IdempotencyKey existing = IdempotencyKey.builder()
                .id(UUID.randomUUID())
                .key(key)
                .userId(userId)
                .requestMethod(requestMethod)
                .requestHash(hash)
                .statusCode(200)
                .responseBody("{\"status\":\"success\"}")
                .createdAt(OffsetDateTime.now(ZoneOffset.UTC))
                .build();

        when(repository.findByKeyAndUserOrClient(key, userId, clientIdentifier)).thenReturn(Optional.of(existing));

        ClaimResult result = idempotencyService.claimOrRetrieve(key, userId, clientIdentifier, requestPath, requestMethod, bodyBytes);

        assertThat(result.isNewClaim()).isFalse();
        assertThat(result.record().getStatusCode()).isEqualTo(200);
        assertThat(result.record().getResponseBody()).isEqualTo("{\"status\":\"success\"}");
    }

    @Test
    @DisplayName("claimOrRetrieve should throw IdempotencyConflictException when key is in-progress")
    void claimOrRetrieve_InProgressKey_ThrowsConflict() {
        String hash = IdempotencyService.computeSha256(bodyBytes);
        IdempotencyKey existing = IdempotencyKey.builder()
                .id(UUID.randomUUID())
                .key(key)
                .userId(userId)
                .requestMethod(requestMethod)
                .requestHash(hash)
                .statusCode(null) // null = in progress
                .build();

        when(repository.findByKeyAndUserOrClient(key, userId, clientIdentifier)).thenReturn(Optional.of(existing));

        assertThatThrownBy(() -> idempotencyService.claimOrRetrieve(key, userId, clientIdentifier, requestPath, requestMethod, bodyBytes))
                .isInstanceOf(IdempotencyConflictException.class)
                .hasMessageContaining("currently being processed");
    }

    @Test
    @DisplayName("claimOrRetrieve should throw IdempotencyPayloadMismatchException when request body differs")
    void claimOrRetrieve_PayloadMismatch_ThrowsBadRequest() {
        String originalHash = IdempotencyService.computeSha256("{\"quantity\":10}".getBytes());
        IdempotencyKey existing = IdempotencyKey.builder()
                .id(UUID.randomUUID())
                .key(key)
                .userId(userId)
                .requestMethod(requestMethod)
                .requestHash(originalHash)
                .statusCode(200)
                .build();

        when(repository.findByKeyAndUserOrClient(key, userId, clientIdentifier)).thenReturn(Optional.of(existing));

        byte[] differentBody = "{\"quantity\":999}".getBytes();

        assertThatThrownBy(() -> idempotencyService.claimOrRetrieve(key, userId, clientIdentifier, requestPath, requestMethod, differentBody))
                .isInstanceOf(IdempotencyPayloadMismatchException.class)
                .hasMessageContaining("does not match the original request hash");
    }

    @Test
    @DisplayName("recordSuccessResponse should update repository with status code and body")
    void recordSuccessResponse_UpdatesRepository() {
        UUID recordId = UUID.randomUUID();
        idempotencyService.recordSuccessResponse(recordId, 201, "{\"id\":\"new-resource\"}");
        verify(repository).updateResponse(recordId, 201, "{\"id\":\"new-resource\"}");
    }
}
