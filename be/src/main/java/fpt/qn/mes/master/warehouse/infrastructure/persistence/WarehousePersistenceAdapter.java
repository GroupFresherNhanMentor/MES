package fpt.qn.mes.master.warehouse.infrastructure.persistence;

import static fpt.qn.mes.jooq.Tables.WAREHOUSES;

import java.util.Optional;
import java.util.UUID;

import org.jooq.DSLContext;
import org.springframework.stereotype.Repository;

import fpt.qn.mes.common.dto.response.PaginationResult;
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

    @Override public Optional<Warehouse> findById(UUID id) { throw new UnsupportedOperationException("Not implemented"); }
    @Override public Warehouse save(Warehouse w) { throw new UnsupportedOperationException("Not implemented"); }
    @Override public Warehouse update(Warehouse w) { throw new UnsupportedOperationException("Not implemented"); }
    @Override public void deleteById(UUID id) {}
    @Override public PaginationResult<Warehouse> findAll(int page, int size) { throw new UnsupportedOperationException("Not implemented"); }
    @Override public boolean existsByCode(String code) { throw new UnsupportedOperationException("Not implemented"); }
}
