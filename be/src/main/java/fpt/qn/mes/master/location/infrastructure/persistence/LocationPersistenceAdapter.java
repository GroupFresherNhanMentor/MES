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

    @Override public Optional<WarehouseLocation> findById(UUID id) { throw new UnsupportedOperationException("Not implemented"); }
    @Override public List<WarehouseLocation> findByWarehouseId(UUID warehouseId) { throw new UnsupportedOperationException("Not implemented"); }
    @Override public WarehouseLocation save(WarehouseLocation l) { throw new UnsupportedOperationException("Not implemented"); }
    @Override public WarehouseLocation update(WarehouseLocation l) { throw new UnsupportedOperationException("Not implemented"); }
    @Override public void deleteById(UUID id) {}
}
