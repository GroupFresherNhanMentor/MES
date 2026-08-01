package fpt.qn.mes.audit.domain.entities;

import java.time.Instant;
import java.util.UUID;

import fpt.qn.mes.common.util.UuidV7;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.experimental.FieldDefaults;

@Getter
@Builder
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AuditLog {

    UUID id;
    UUID actorId;
    AuditAction action;
    String entityType;
    UUID entityId;
    String oldValue;
    String newValue;
    String ipAddress;
    Instant createdAt;

    public static AuditLog create(
            UUID actorId,
            AuditAction action,
            String entityType,
            UUID entityId,
            String oldValue,
            String newValue,
            String ipAddress) {
        return AuditLog.builder()
                .id(UuidV7.generate())
                .actorId(actorId)
                .action(action)
                .entityType(entityType)
                .entityId(entityId)
                .oldValue(oldValue)
                .newValue(newValue)
                .ipAddress(ipAddress)
                .createdAt(Instant.now())
                .build();
    }
}
