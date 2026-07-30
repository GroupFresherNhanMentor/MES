package fpt.qn.mes.common.idempotency.domain.entities;

import java.time.OffsetDateTime;
import java.util.UUID;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class IdempotencyKey {

    UUID id;
    String key;
    UUID userId;
    String clientIdentifier;
    String requestPath;
    String requestMethod;
    String requestHash;
    Integer statusCode;
    String responseBody;
    OffsetDateTime createdAt;
    OffsetDateTime updatedAt;

    public boolean isCompleted() {
        return statusCode != null;
    }

    public boolean isInProgress() {
        return statusCode == null;
    }
}
