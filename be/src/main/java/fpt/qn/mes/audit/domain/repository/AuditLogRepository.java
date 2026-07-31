package fpt.qn.mes.audit.domain.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import fpt.qn.mes.audit.domain.entities.AuditLog;
import fpt.qn.mes.audit.domain.repository.criteria.AuditLogSearchCriteria;

public interface AuditLogRepository {

    AuditLog save(AuditLog auditLog);

    Optional<AuditLog> findById(UUID id);

    List<AuditLog> search(AuditLogSearchCriteria criteria);

    long count(AuditLogSearchCriteria criteria);
}
