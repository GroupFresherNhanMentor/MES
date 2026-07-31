package fpt.qn.mes.audit.application.port.out;

import java.util.UUID;

import fpt.qn.mes.audit.domain.entities.AuditAction;
import fpt.qn.mes.audit.domain.entities.AuditLog;

public interface AuditLogPort {

    AuditLog log(
            UUID actorId,
            AuditAction action,
            String entityType,
            UUID entityId,
            String oldValue,
            String newValue,
            String ipAddress);

    AuditLog log(
            AuditAction action,
            String entityType,
            UUID entityId,
            String oldValue,
            String newValue);
}
