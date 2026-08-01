package fpt.qn.mes.bom.infrastructure.persistence.bomstatus;

import static fpt.qn.mes.jooq.Tables.BOM_STATUSES;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.jooq.DSLContext;
import org.springframework.stereotype.Repository;

import fpt.qn.mes.bom.domain.entities.BomStatus;
import fpt.qn.mes.bom.domain.repository.BomStatusRepository;
import fpt.qn.mes.common.repository.BaseRepository;
import fpt.qn.mes.jooq.tables.records.BomStatusesRecord;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;

@Repository
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class BomStatusPersistenceAdapter extends BaseRepository<BomStatusesRecord> implements BomStatusRepository {

    BomStatusRecordMapper mapper;

    public BomStatusPersistenceAdapter(DSLContext ctx, BomStatusRecordMapper mapper) {
        super(ctx, BOM_STATUSES);
        this.mapper = mapper;
    }

    @Override
    public Optional<BomStatus> findById(UUID id) {
        return ctx.selectFrom(BOM_STATUSES)
                .where(BOM_STATUSES.ID.eq(id))
                .fetchOptional(r -> mapper.toDomain(r));
    }

    @Override
    public Optional<BomStatus> findByName(String name) {
        return ctx.selectFrom(BOM_STATUSES)
                .where(BOM_STATUSES.NAME.eq(name))
                .fetchOptional(r -> mapper.toDomain(r));
    }

    @Override
    public BomStatus save(BomStatus bomStatus) {
        BomStatusesRecord r = mapper.toRecord(bomStatus);
        ctx.insertInto(BOM_STATUSES).set(r).onConflict(BOM_STATUSES.ID).doUpdate().set(r).execute();
        return bomStatus;
    }

    @Override
    public boolean existsById(UUID id) {
        return ctx.fetchExists(BOM_STATUSES, BOM_STATUSES.ID.eq(id));
    }

    @Override
    public boolean existsByName(String name) {
        return ctx.fetchExists(BOM_STATUSES, BOM_STATUSES.NAME.eq(name));
    }

    @Override
    public List<BomStatus> findAll() {
        return ctx.selectFrom(BOM_STATUSES)
                .orderBy(BOM_STATUSES.NAME.asc())
                .fetch(r -> mapper.toDomain(r));
    }
}
