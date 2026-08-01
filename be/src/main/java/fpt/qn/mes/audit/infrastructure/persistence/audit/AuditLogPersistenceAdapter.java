package fpt.qn.mes.audit.infrastructure.persistence.audit;

import static fpt.qn.mes.jooq.Tables.AUDIT_LOGS;

import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.OrderField;
import org.jooq.impl.DSL;
import org.springframework.stereotype.Repository;

import fpt.qn.mes.audit.domain.entities.AuditLog;
import fpt.qn.mes.audit.domain.repository.AuditLogRepository;
import fpt.qn.mes.audit.domain.repository.criteria.AuditLogSearchCriteria;
import fpt.qn.mes.common.repository.BaseRepository;
import fpt.qn.mes.jooq.tables.records.AuditLogsRecord;

@Repository
public class AuditLogPersistenceAdapter extends BaseRepository<AuditLogsRecord> implements AuditLogRepository {

    private final AuditLogRecordMapper mapper;

    public AuditLogPersistenceAdapter(DSLContext ctx, AuditLogRecordMapper mapper) {
        super(ctx, AUDIT_LOGS);
        this.mapper = mapper;
    }

    @Override
    public AuditLog save(AuditLog auditLog) {
        AuditLogsRecord record = ctx.newRecord(AUDIT_LOGS);
        mapper.toRecord(auditLog, record);
        record.store();
        return mapper.toDomain(record);
    }

    @Override
    public Optional<AuditLog> findById(UUID id) {
        return Optional.ofNullable(
                ctx.selectFrom(AUDIT_LOGS)
                   .where(AUDIT_LOGS.ID.eq(id))
                   .fetchOne()
        ).map(mapper::toDomain);
    }

    @Override
    public List<AuditLog> search(AuditLogSearchCriteria criteria) {
        Condition condition = buildCondition(criteria);
        List<OrderField<?>> orderFields = buildSort(criteria);

        int page = criteria != null ? criteria.getPage() : 0;
        int size = criteria != null && criteria.getSize() > 0 ? criteria.getSize() : 20;

        return ctx.selectFrom(AUDIT_LOGS)
                .where(condition)
                .orderBy(orderFields)
                .limit(size)
                .offset(page * size)
                .fetch(mapper::toDomain);
    }

    @Override
    public long count(AuditLogSearchCriteria criteria) {
        Condition condition = buildCondition(criteria);
        Long total = ctx.selectCount()
                .from(AUDIT_LOGS)
                .where(condition)
                .fetchOne(0, Long.class);
        return total != null ? total : 0L;
    }

    public void deleteById(UUID id) {
        throw new UnsupportedOperationException("Audit logs are immutable and cannot be updated or deleted");
    }

    private Condition buildCondition(AuditLogSearchCriteria criteria) {
        if (criteria == null) {
            return DSL.noCondition();
        }
        Condition condition = DSL.noCondition();

        if (criteria.getEntityType() != null && !criteria.getEntityType().isBlank()) {
            condition = condition.and(AUDIT_LOGS.ENTITY_TYPE.eq(criteria.getEntityType()));
        }
        if (criteria.getEntityId() != null) {
            condition = condition.and(AUDIT_LOGS.ENTITY_ID.eq(criteria.getEntityId()));
        }
        if (criteria.getActorId() != null) {
            condition = condition.and(AUDIT_LOGS.ACTOR_ID.eq(criteria.getActorId()));
        }
        if (criteria.getAction() != null) {
            condition = condition.and(AUDIT_LOGS.ACTION.eq(criteria.getAction().name()));
        }
        if (criteria.getFrom() != null) {
            condition = condition.and(AUDIT_LOGS.CREATED_AT.ge(criteria.getFrom().atOffset(ZoneOffset.UTC)));
        }
        if (criteria.getTo() != null) {
            condition = condition.and(AUDIT_LOGS.CREATED_AT.le(criteria.getTo().atOffset(ZoneOffset.UTC)));
        }

        return condition;
    }

    private List<OrderField<?>> buildSort(AuditLogSearchCriteria criteria) {
        List<OrderField<?>> orderFields = new ArrayList<>();
        if (criteria != null && criteria.getSort() != null && !criteria.getSort().isEmpty()) {
            for (String sortParam : criteria.getSort()) {
                String[] parts = sortParam.split(",");
                String property = parts[0].trim();
                boolean isAsc = parts.length < 2 || "asc".equalsIgnoreCase(parts[1].trim());

                if ("createdAt".equals(property)) {
                    orderFields.add(isAsc ? AUDIT_LOGS.CREATED_AT.asc() : AUDIT_LOGS.CREATED_AT.desc());
                } else if ("action".equals(property)) {
                    orderFields.add(isAsc ? AUDIT_LOGS.ACTION.asc() : AUDIT_LOGS.ACTION.desc());
                } else if ("entityType".equals(property)) {
                    orderFields.add(isAsc ? AUDIT_LOGS.ENTITY_TYPE.asc() : AUDIT_LOGS.ENTITY_TYPE.desc());
                }
            }
        }
        if (orderFields.isEmpty()) {
            orderFields.add(AUDIT_LOGS.CREATED_AT.desc());
        }
        return orderFields;
    }
}
