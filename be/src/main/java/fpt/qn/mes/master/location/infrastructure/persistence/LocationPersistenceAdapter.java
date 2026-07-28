package fpt.qn.mes.master.location.infrastructure.persistence;

import static fpt.qn.mes.jooq.Tables.WAREHOUSE_LOCATIONS;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.jooq.DSLContext;
import org.springframework.stereotype.Repository;

import fpt.qn.mes.common.repository.BaseRepository;
import fpt.qn.mes.jooq.tables.records.WarehouseLocationsRecord;
import fpt.qn.mes.master.location.domain.entities.WarehouseLocation;
import fpt.qn.mes.master.location.domain.repository.WarehouseLocationRepository;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;

@Repository
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class LocationPersistenceAdapter extends BaseRepository<WarehouseLocationsRecord> implements WarehouseLocationRepository {

    LocationRecordMapper mapper;

    public LocationPersistenceAdapter(DSLContext ctx, LocationRecordMapper mapper) {
        super(ctx, WAREHOUSE_LOCATIONS); this.mapper = mapper;
    }

    @Override
    public Optional<WarehouseLocation> findById(UUID id) {
        return fetchById(id).map(mapper::toDomain);
    }

    @Override
    public List<WarehouseLocation> findByWarehouseId(UUID warehouseId) {
        return ctx.selectFrom(WAREHOUSE_LOCATIONS)
                .where(WAREHOUSE_LOCATIONS.WAREHOUSE_ID.eq(warehouseId))
                .fetch().stream().map(mapper::toDomain).toList();
    }

    @Override
    public WarehouseLocation save(WarehouseLocation location) {
        return mapper.toDomain(create(mapper.toRecord(location)));
    }

    @Override
    public WarehouseLocation update(WarehouseLocation location) {
        return mapper.toDomain(update(mapper.toRecord(location)));
    }

    @Override
    public void deleteById(UUID id) {}

    public boolean existsByWarehouseIdAndCode(UUID warehouseId, String code) {
        return ctx.fetchExists(ctx.selectFrom(WAREHOUSE_LOCATIONS)
                .where(WAREHOUSE_LOCATIONS.WAREHOUSE_ID.eq(warehouseId))
                .and(WAREHOUSE_LOCATIONS.CODE.eq(code)));
    }
}
