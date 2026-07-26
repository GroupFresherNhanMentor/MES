package fpt.qn.mes.inventory.infrastructure.persistence;

import static fpt.qn.mes.jooq.Tables.STOCK_LOTS;

import java.util.Optional;
import java.util.UUID;

import org.jooq.DSLContext;
import org.springframework.stereotype.Repository;

import fpt.qn.mes.common.dto.PaginationResult;
import fpt.qn.mes.common.repository.BaseRepository;
import fpt.qn.mes.inventory.domain.entities.StockLot;
import fpt.qn.mes.inventory.domain.repository.StockLotRepository;
import fpt.qn.mes.jooq.tables.records.StockLotsRecord;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;

@Repository
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class StockLotPersistenceAdapter extends BaseRepository<StockLotsRecord> implements StockLotRepository {

    InventoryRecordMapper mapper;

    public StockLotPersistenceAdapter(DSLContext ctx, InventoryRecordMapper mapper) {
        super(ctx, STOCK_LOTS); this.mapper = mapper;
    }

    @Override public Optional<StockLot> findById(UUID id) { throw new UnsupportedOperationException("Not implemented"); }
    @Override public StockLot save(StockLot lot) { throw new UnsupportedOperationException("Not implemented"); }
    @Override public PaginationResult<StockLot> findAll(int page, int size) { throw new UnsupportedOperationException("Not implemented"); }
}
