package fpt.qn.mes.master.warehouse.infrastructure.persistence;

import static fpt.qn.mes.jooq.Tables.WAREHOUSES;

import java.util.Optional;
import java.util.UUID;

import org.jooq.DSLContext;
import org.springframework.stereotype.Repository;

import fpt.qn.mes.common.domainQuery.PaginationResult;
import fpt.qn.mes.common.repository.BaseRepository;
import fpt.qn.mes.jooq.tables.records.WarehousesRecord;
import fpt.qn.mes.master.warehouse.domain.entities.Warehouse;
import fpt.qn.mes.master.warehouse.domain.repository.WarehouseRepository;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;

@Repository
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class WarehousePersistenceAdapter extends BaseRepository<WarehousesRecord> implements WarehouseRepository {

    WarehouseRecordMapper mapper;

    public WarehousePersistenceAdapter(DSLContext ctx, WarehouseRecordMapper mapper) {
        super(ctx, WAREHOUSES); this.mapper = mapper;
    }

    @Override
    public Optional<Warehouse> findById(UUID id) {
        return fetchById(id).map(mapper::toDomain);
    }

    @Override
    public java.util.List<Warehouse> findByIds(java.util.Collection<UUID> ids) {
        if (ids == null || ids.isEmpty()) return java.util.List.of();
        return ctx.selectFrom(WAREHOUSES)
                .where(WAREHOUSES.ID.in(ids))
                .fetch().map(mapper::toDomain);
    }

    @Override
    public Warehouse save(Warehouse w) {
        return mapper.toDomain(create(mapper.toRecord(w)));
    }

    @Override
    public Warehouse update(Warehouse w) {
        return mapper.toDomain(update(mapper.toRecord(w)));
    }

    @Override
    public void deleteById(UUID id) {}

    @Override
    public PaginationResult<Warehouse> findAll(int page, int size) {
        var records = ctx.selectFrom(WAREHOUSES).orderBy(WAREHOUSES.CREATED_AT.desc())
                .limit(size).offset((long) page * size).fetch();
        int total = ctx.fetchCount(ctx.selectFrom(WAREHOUSES));
        return PaginationResult.<Warehouse>builder().total(total).items(records.stream().map(r -> mapper.toDomain(r)).toList()).build();
    }

    @Override
    public PaginationResult<Warehouse> findAllByStatus(int page, int size, UUID statusId) {
        var records = ctx.selectFrom(WAREHOUSES)
                .where(WAREHOUSES.WAREHOUSE_STATUS_ID.eq(statusId))
                .orderBy(WAREHOUSES.CREATED_AT.desc())
                .limit(size).offset((long) page * size).fetch();
        int total = ctx.fetchCount(ctx.selectFrom(WAREHOUSES)
                .where(WAREHOUSES.WAREHOUSE_STATUS_ID.eq(statusId)));
        return PaginationResult.<Warehouse>builder().total(total).items(records.stream().map(r -> mapper.toDomain(r)).toList()).build();
    }

    @Override
    public boolean existsByCode(String code) {
        return ctx.fetchExists(ctx.selectFrom(WAREHOUSES).where(WAREHOUSES.CODE.eq(code)));
    }
}
