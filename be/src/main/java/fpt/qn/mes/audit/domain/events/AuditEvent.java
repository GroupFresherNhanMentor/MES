package fpt.qn.mes.audit.domain.events;

import java.time.Instant;
import java.util.UUID;

import fpt.qn.mes.audit.domain.entities.AuditAction;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.experimental.FieldDefaults;

@Getter
@Builder
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AuditEvent {

    UUID actorId;
    AuditAction action;
    String entityType;
    UUID entityId;
    String oldValue;
    String newValue;
    String ipAddress;
    Instant timestamp;

    public static AuditEvent create(
            UUID actorId,
            AuditAction action,
            String entityType,
            UUID entityId,
            String oldValue,
            String newValue,
            String ipAddress) {
        return AuditEvent.builder()
                .actorId(actorId)
                .action(action)
                .entityType(entityType)
                .entityId(entityId)
                .oldValue(oldValue)
                .newValue(newValue)
                .ipAddress(ipAddress)
                .timestamp(Instant.now())
                .build();
    }

    public static AuditEvent create(
            AuditAction action,
            String entityType,
            UUID entityId,
            String oldValue,
            String newValue) {
        return create(null, action, entityType, entityId, oldValue, newValue, null);
    }
}
