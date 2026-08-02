package fpt.qn.mes.audit.application.dto;

import java.time.Instant;
import java.util.List;
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
public class AuditLogSearchRequest {

    String entityType;
    UUID entityId;
    UUID actorId;
    AuditAction action;
    Instant from;
    Instant to;

    @Builder.Default
    int page = 0;

    @Builder.Default
    int size = 20;

    List<String> sort;
}
