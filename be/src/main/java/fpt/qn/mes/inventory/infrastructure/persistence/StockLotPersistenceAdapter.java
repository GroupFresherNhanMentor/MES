package fpt.qn.mes.inventory.infrastructure.persistence;

import static fpt.qn.mes.jooq.Tables.STOCK_LOTS;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.jooq.DSLContext;
import org.springframework.stereotype.Repository;

import fpt.qn.mes.common.dto.response.PaginationResult;
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
        super(ctx, STOCK_LOTS);
        this.mapper = mapper;
    }

    @Override
    public Optional<StockLot> findById(UUID id) {
        StockLotsRecord record = ctx.selectFrom(STOCK_LOTS)
                .where(STOCK_LOTS.ID.eq(id))
                .fetchOne();
        return Optional.ofNullable(mapper.toDomain(record));
    }

    @Override
    public StockLot save(StockLot lot) {
        StockLotsRecord record = mapper.toRecord(lot);
        if (record.getId() == null) {
            record.setId(UUID.randomUUID());
        }
        ctx.attach(record);
        record.store();
        return mapper.toDomain(record);
    }

    @Override
    public PaginationResult<StockLot> findAll(int page, int size) {
        long totalElements = ctx.fetchCount(STOCK_LOTS);

        List<StockLot> content = ctx.selectFrom(STOCK_LOTS)
                .orderBy(STOCK_LOTS.CREATED_AT.desc())
                .limit(size)
                .offset(page * size)
                .fetch()
                .map(mapper::toDomain);

        return new PaginationResult<>(totalElements, content);
    }
}
