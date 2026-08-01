package fpt.qn.mes.audit.domain.repository.criteria;

import java.time.Instant;
import java.util.UUID;
import fpt.qn.mes.audit.domain.entities.AuditAction;
import fpt.qn.mes.common.domainQuery.BaseSearchCriteria;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AuditLogSearchCriteria extends BaseSearchCriteria {

    String entityType;
    UUID entityId;
    UUID actorId;
    AuditAction action;
    Instant from;
    Instant to;
}
