package fpt.qn.mes.audit.infrastructure.persistence.audit;

import java.time.ZoneOffset;
import org.springframework.stereotype.Component;
import fpt.qn.mes.audit.domain.entities.AuditAction;
import fpt.qn.mes.audit.domain.entities.AuditLog;
import fpt.qn.mes.jooq.tables.records.AuditLogsRecord;

@Component
public class AuditLogRecordMapper {

    public AuditLog toDomain(AuditLogsRecord record) {
        if (record == null) {
            return null;
        }
        AuditAction action = null;
        if (record.getAction() != null) {
            try {
                action = AuditAction.valueOf(record.getAction());
            } catch (IllegalArgumentException ignored) {
            }
        }
        return AuditLog.builder()
                .id(record.getId())
                .actorId(record.getActorId())
                .action(action)
                .entityType(record.getEntityType())
                .entityId(record.getEntityId())
                .oldValue(record.getOldValue() != null ? record.getOldValue().data() : null)
                .newValue(record.getNewValue() != null ? record.getNewValue().data() : null)
                .ipAddress(record.getIpAddress())
                .createdAt(record.getCreatedAt() != null ? record.getCreatedAt().toInstant() : null)
                .build();
    }

    public void toRecord(AuditLog domain, AuditLogsRecord record) {
        if (domain == null || record == null) {
            return;
        }
        record.setId(domain.getId());
        record.setActorId(domain.getActorId());
        record.setAction(domain.getAction() != null ? domain.getAction().name() : null);
        record.setEntityType(domain.getEntityType());
        record.setEntityId(domain.getEntityId());
        record.setOldValue(domain.getOldValue() != null ? org.jooq.JSONB.valueOf(domain.getOldValue()) : null);
        record.setNewValue(domain.getNewValue() != null ? org.jooq.JSONB.valueOf(domain.getNewValue()) : null);
        record.setIpAddress(domain.getIpAddress());
        record.setCreatedAt(domain.getCreatedAt() != null ? domain.getCreatedAt().atOffset(ZoneOffset.UTC) : null);
    }
}
