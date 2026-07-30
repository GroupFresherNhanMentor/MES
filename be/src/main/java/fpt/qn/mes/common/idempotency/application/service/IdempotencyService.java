package fpt.qn.mes.common.idempotency.application.service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.HexFormat;
import java.util.Optional;
import java.util.UUID;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import fpt.qn.mes.common.idempotency.application.exception.IdempotencyConflictException;
import fpt.qn.mes.common.idempotency.application.exception.IdempotencyPayloadMismatchException;
import fpt.qn.mes.common.idempotency.domain.entities.IdempotencyKey;
import fpt.qn.mes.common.idempotency.domain.repository.IdempotencyKeyRepository;
import fpt.qn.mes.common.util.UuidV7;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class IdempotencyService {

    public record ClaimResult(IdempotencyKey record, boolean isNewClaim) {}

    IdempotencyKeyRepository repository;

    @Transactional
    public ClaimResult claimOrRetrieve(String key, UUID userId, String clientIdentifier, String requestPath, String requestMethod, byte[] requestBody) {
        String requestHash = computeSha256(requestBody);

        Optional<IdempotencyKey> existingOpt = repository.findByKeyAndUserOrClient(key, userId, clientIdentifier);
        if (existingOpt.isPresent()) {
            IdempotencyKey existing = existingOpt.get();
            if (!existing.getRequestHash().equals(requestHash)) {
                throw new IdempotencyPayloadMismatchException("Request payload body does not match the original request hash for this idempotency key");
            }
            if (existing.isInProgress()) {
                throw new IdempotencyConflictException("A request with this idempotency key is currently being processed");
            }
            return new ClaimResult(existing, false);
        }

        IdempotencyKey newRecord = IdempotencyKey.builder()
                .id(UuidV7.generate())
                .key(key)
                .userId(userId)
                .clientIdentifier(clientIdentifier)
                .requestPath(requestPath)
                .requestMethod(requestMethod)
                .requestHash(requestHash)
                .statusCode(null)
                .responseBody(null)
                .createdAt(OffsetDateTime.now(ZoneOffset.UTC))
                .updatedAt(OffsetDateTime.now(ZoneOffset.UTC))
                .build();

        try {
            IdempotencyKey saved = repository.save(newRecord);
            return new ClaimResult(saved, true);
        } catch (DataIntegrityViolationException e) {
            throw new IdempotencyConflictException("A request with this idempotency key is currently being processed");
        }
    }

    @Transactional
    public void recordSuccessResponse(UUID id, int statusCode, String responseBody) {
        if (id != null) {
            repository.updateResponse(id, statusCode, responseBody);
        }
    }

    @Transactional
    public void releaseLockOnFailure(UUID id) {
        if (id != null) {
            repository.deleteById(id);
        }
    }

    public static String computeSha256(byte[] body) {
        if (body == null) {
            body = new byte[0];
        }
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(body);
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 algorithm not available", e);
        }
    }
}
