package fpt.qn.mes.master.machine.infrastructure.persistence;

import static fpt.qn.mes.jooq.Tables.MACHINES;

import java.util.Optional;
import java.util.UUID;

import org.jooq.DSLContext;
import org.springframework.stereotype.Repository;

import fpt.qn.mes.common.domainQuery.PaginationResult;
import fpt.qn.mes.common.repository.BaseRepository;
import fpt.qn.mes.jooq.tables.records.MachinesRecord;
import fpt.qn.mes.master.machine.domain.entities.Machine;
import fpt.qn.mes.master.machine.domain.repository.MachineRepository;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;

@Repository
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class MachinePersistenceAdapter extends BaseRepository<MachinesRecord> implements MachineRepository {

    MachineRecordMapper mapper;

    public MachinePersistenceAdapter(DSLContext ctx, MachineRecordMapper mapper) {
        super(ctx, MACHINES); this.mapper = mapper;
    }

    @Override
    public Optional<Machine> findById(UUID id) { return fetchById(id).map(mapper::toDomain); }
    @Override
    public Machine save(Machine m) { return mapper.toDomain(create(mapper.toRecord(m))); }
    @Override
    public Machine update(Machine m) { return mapper.toDomain(update(mapper.toRecord(m))); }
    @Override
    public void deleteById(UUID id) {}

    @Override
    public PaginationResult<Machine> findAll(int page, int size) {
        var records = ctx.selectFrom(MACHINES).orderBy(MACHINES.CREATED_AT.desc())
                .limit(size).offset((long) page * size).fetch();
        int total = ctx.fetchCount(ctx.selectFrom(MACHINES));
        return PaginationResult.<Machine>builder().total(total).items(records.stream().map(r -> mapper.toDomain(r)).toList()).build();
    }

    @Override
    public PaginationResult<Machine> findAllByStatus(int page, int size, UUID statusId) {
        var records = ctx.selectFrom(MACHINES)
                .where(MACHINES.MACHINE_STATUS_ID.eq(statusId))
                .orderBy(MACHINES.CREATED_AT.desc())
                .limit(size).offset((long) page * size).fetch();
        int total = ctx.fetchCount(ctx.selectFrom(MACHINES)
                .where(MACHINES.MACHINE_STATUS_ID.eq(statusId)));
        return PaginationResult.<Machine>builder().total(total).items(records.stream().map(r -> mapper.toDomain(r)).toList()).build();
    }

    @Override
    public boolean existsByCode(String code) {
        return ctx.fetchExists(ctx.selectFrom(MACHINES).where(MACHINES.CODE.eq(code)));
    }
}
