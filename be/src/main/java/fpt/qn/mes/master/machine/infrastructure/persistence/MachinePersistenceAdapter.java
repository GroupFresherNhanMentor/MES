package fpt.qn.mes.master.machine.infrastructure.persistence;

import static fpt.qn.mes.jooq.Tables.MACHINES;
import static fpt.qn.mes.jooq.Tables.MACHINE_STATUSES;
import static fpt.qn.mes.jooq.Tables.PRODUCTION_LINES;
import static fpt.qn.mes.jooq.Tables.USERS;

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
import fpt.qn.mes.jooq.tables.Users;
import fpt.qn.mes.jooq.tables.records.MachinesRecord;
import fpt.qn.mes.master.machine.domain.entities.Machine;
import fpt.qn.mes.master.machine.domain.repository.MachineRepository;
import fpt.qn.mes.master.machine.domain.repository.criteria.MachineSearchCriteria;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;

@Repository
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class MachinePersistenceAdapter extends BaseRepository<MachinesRecord> implements MachineRepository {

    private static final Users CREATOR = USERS.as("creator");
    private static final Users UPDATER = USERS.as("updater");

    private static final Map<String, Field<?>> SORT_FIELDS = Map.of(
            "name", MACHINES.NAME,
            "code", MACHINES.CODE,
            "created_at", MACHINES.CREATED_AT);

    private static final Field<?> DEFAULT_SORT_FIELD = MACHINES.CREATED_AT;

    MachineRecordMapper mapper;

    public MachinePersistenceAdapter(DSLContext ctx, MachineRecordMapper mapper) {
        super(ctx, MACHINES);
        this.mapper = mapper;
    }

    @Override
    public Optional<Machine> findById(UUID id) {
        return ctx.select()
                .from(MACHINES)
                .leftJoin(MACHINE_STATUSES).on(MACHINES.MACHINE_STATUS_ID.eq(MACHINE_STATUSES.ID))
                .leftJoin(PRODUCTION_LINES).on(MACHINES.PRODUCTION_LINE_ID.eq(PRODUCTION_LINES.ID))
                .leftJoin(CREATOR).on(MACHINES.CREATED_BY.eq(CREATOR.ID))
                .leftJoin(UPDATER).on(MACHINES.UPDATED_BY.eq(UPDATER.ID))
                .where(MACHINES.ID.eq(id))
                .fetchOptional(r -> mapper.toDomain(
                        r.into(MACHINES), r.into(MACHINE_STATUSES), r.into(PRODUCTION_LINES),
                        r.into(CREATOR), r.into(UPDATER)));
    }

    @Override
    public Machine save(Machine m) {
        MachinesRecord r = mapper.toRecord(m);
        ctx.insertInto(MACHINES).set(r).onConflict(MACHINES.ID).doUpdate().set(r).execute();
        return m;
    }

    @Override
    public Machine update(Machine m) {
        MachinesRecord r = mapper.toRecord(m);
        ctx.update(MACHINES).set(r).where(MACHINES.ID.eq(m.getId())).execute();
        return m;
    }

    @Override
    public PaginationResult<Machine> search(MachineSearchCriteria criteria) {
        Condition condition = buildCondition(criteria);
        List<SortField<?>> orderBy = SortUtils.resolveSorts(criteria.getSort(), SORT_FIELDS, DEFAULT_SORT_FIELD);
        long total = ctx.fetchCount(MACHINES, condition);
        List<Machine> items = ctx.select()
                .from(MACHINES)
                .leftJoin(MACHINE_STATUSES).on(MACHINES.MACHINE_STATUS_ID.eq(MACHINE_STATUSES.ID))
                .leftJoin(PRODUCTION_LINES).on(MACHINES.PRODUCTION_LINE_ID.eq(PRODUCTION_LINES.ID))
                .leftJoin(CREATOR).on(MACHINES.CREATED_BY.eq(CREATOR.ID))
                .leftJoin(UPDATER).on(MACHINES.UPDATED_BY.eq(UPDATER.ID))
                .where(condition)
                .orderBy(orderBy)
                .limit(criteria.getSize())
                .offset((long) criteria.getPage() * criteria.getSize())
                .fetch(r -> mapper.toDomain(
                        r.into(MACHINES), r.into(MACHINE_STATUSES), r.into(PRODUCTION_LINES),
                        r.into(CREATOR), r.into(UPDATER)));
        return PaginationResult.<Machine>builder().total(total).items(items).build();
    }

    @Override
    public boolean existsByCode(String code) {
        return ctx.fetchExists(MACHINES, MACHINES.CODE.eq(code));
    }

    private Condition buildCondition(MachineSearchCriteria criteria) {
        Condition condition = DSL.noCondition();
        if (criteria.getProductionLineId() != null) {
            condition = condition.and(MACHINES.PRODUCTION_LINE_ID.eq(criteria.getProductionLineId()));
        }
        if (criteria.getMachineStatusId() != null) {
            condition = condition.and(MACHINES.MACHINE_STATUS_ID.eq(criteria.getMachineStatusId()));
        }
        if (criteria.getCode() != null && !criteria.getCode().isBlank()) {
    condition = condition.and(MACHINES.CODE.containsIgnoreCase(criteria.getCode())
        .or(MACHINES.NAME.containsIgnoreCase(criteria.getCode())));
} else if (criteria.getName() != null && !criteria.getName().isBlank()) {
    condition = condition.and(MACHINES.NAME.containsIgnoreCase(criteria.getName()));
}
        return condition;
    }
}
