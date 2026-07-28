package fpt.qn.mes.inventory.infrastructure.persistence;

import static fpt.qn.mes.jooq.Tables.STOCK_MOVEMENTS;

import java.util.List;
import java.util.UUID;

import org.jooq.DSLContext;
import org.springframework.stereotype.Repository;

import fpt.qn.mes.common.dto.response.PaginationResult;
import fpt.qn.mes.common.repository.BaseRepository;
import fpt.qn.mes.inventory.domain.entities.StockMovement;
import fpt.qn.mes.inventory.domain.repository.StockMovementRepository;
import fpt.qn.mes.jooq.tables.records.StockMovementsRecord;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;

@Repository
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class StockMovementPersistenceAdapter extends BaseRepository<StockMovementsRecord> implements StockMovementRepository {

    InventoryRecordMapper mapper;

    public StockMovementPersistenceAdapter(DSLContext ctx, InventoryRecordMapper mapper) {
        super(ctx, STOCK_MOVEMENTS);
        this.mapper = mapper;
    }

    @Override
    public StockMovement save(StockMovement movement) {
        StockMovementsRecord record = mapper.toRecord(movement);
        if (record.getId() == null) {
            record.setId(UUID.randomUUID());
        }
        ctx.attach(record);
        record.store();
        return mapper.toDomain(record);
    }

    @Override
    public PaginationResult<StockMovement> findAll(int page, int size) {
        long totalElements = ctx.fetchCount(STOCK_MOVEMENTS);

        List<StockMovement> content = ctx.selectFrom(STOCK_MOVEMENTS)
                .orderBy(STOCK_MOVEMENTS.CREATED_AT.desc())
                .limit(size)
                .offset(page * size)
                .fetch()
                .map(mapper::toDomain);

        return new PaginationResult<>(totalElements, content);
    }
}
