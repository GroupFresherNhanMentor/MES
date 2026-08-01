package fpt.qn.mes.master.machine.infrastructure.persistence;

import static fpt.qn.mes.jooq.Tables.MACHINE_STATUSES;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.SortField;
import org.jooq.impl.DSL;
import org.springframework.stereotype.Repository;

import fpt.qn.mes.common.domainQuery.PaginationResult;
import fpt.qn.mes.common.repository.BaseRepository;
import fpt.qn.mes.common.repository.SortUtils;
import fpt.qn.mes.jooq.tables.records.MachineStatusesRecord;
import fpt.qn.mes.master.machine.domain.entities.MachineStatus;
import fpt.qn.mes.master.machine.domain.repository.MachineStatusRepository;
import fpt.qn.mes.master.machine.domain.repository.criteria.MachineStatusSearchCriteria;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;

@Repository
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class MachineStatusPersistenceAdapter extends BaseRepository<MachineStatusesRecord> implements MachineStatusRepository {

    private static final Map<String, Field<?>> SORT_FIELDS = Map.of(
            "name", MACHINE_STATUSES.NAME,
            "created_at", MACHINE_STATUSES.CREATED_AT);

    private static final Field<?> DEFAULT_SORT_FIELD = MACHINE_STATUSES.CREATED_AT;

    MachineStatusRecordMapper mapper;

    public MachineStatusPersistenceAdapter(DSLContext ctx, MachineStatusRecordMapper mapper) {
        super(ctx, MACHINE_STATUSES);
        this.mapper = mapper;
    }

    @Override
    public Optional<MachineStatus> findById(UUID id) {
        return ctx.selectFrom(MACHINE_STATUSES)
                .where(MACHINE_STATUSES.ID.eq(id))
                .fetchOptional(r -> mapper.toDomain(r));
    }

    @Override
    public Optional<MachineStatus> findByName(String name) {
        return ctx.selectFrom(MACHINE_STATUSES)
                .where(MACHINE_STATUSES.NAME.eq(name))
                .fetchOptional(r -> mapper.toDomain(r));
    }

    @Override
    public boolean existsByName(String name) {
        return ctx.fetchExists(MACHINE_STATUSES, MACHINE_STATUSES.NAME.eq(name));
    }

    @Override
    public MachineStatus save(MachineStatus status) {
        MachineStatusesRecord r = mapper.toRecord(status);
        ctx.insertInto(MACHINE_STATUSES).set(r).onConflict(MACHINE_STATUSES.ID).doUpdate().set(r).execute();
        return status;
    }

    @Override
    public PaginationResult<MachineStatus> search(MachineStatusSearchCriteria criteria) {
        Condition condition = buildCondition(criteria);
        List<SortField<?>> orderBy = SortUtils.resolveSorts(criteria.getSort(), SORT_FIELDS, DEFAULT_SORT_FIELD);
        long total = ctx.fetchCount(MACHINE_STATUSES, condition);
        List<MachineStatus> items = ctx.selectFrom(MACHINE_STATUSES)
                .where(condition)
                .orderBy(orderBy)
                .limit(criteria.getSize())
                .offset((long) criteria.getPage() * criteria.getSize())
                .fetch(r -> mapper.toDomain(r));
        return PaginationResult.<MachineStatus>builder().total(total).items(items).build();
    }

    private Condition buildCondition(MachineStatusSearchCriteria criteria) {
        Condition condition = DSL.noCondition();
        if (criteria.getName() != null && !criteria.getName().isBlank()) {
            condition = condition.and(MACHINE_STATUSES.NAME.containsIgnoreCase(criteria.getName()));
        }
        return condition;
    }
}
