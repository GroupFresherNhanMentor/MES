package fpt.qn.mes.audit.application.dto;

import java.time.Instant;
import java.util.UUID;

import fpt.qn.mes.audit.domain.entities.AuditAction;
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
public class AuditLogResponse {

    UUID id;
    UUID actorId;
    AuditAction action;
    String entityType;
    UUID entityId;
    String oldValue;
    String newValue;
    String ipAddress;
    Instant createdAt;
}
