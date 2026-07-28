package fpt.qn.mes.inventory.infrastructure.persistence;

import static fpt.qn.mes.jooq.Tables.STOCK_MOVEMENTS;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.jooq.Condition;
import org.jooq.DSLContext;
import org.springframework.stereotype.Repository;

import fpt.qn.mes.common.dto.response.PageResponse;
import fpt.qn.mes.common.repository.BaseRepository;
import fpt.qn.mes.inventory.domain.entities.StockMovement;
import fpt.qn.mes.inventory.domain.repository.StockMovementRepository;
import fpt.qn.mes.inventory.domain.repository.StockMovementSearchCriteria;
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
    public PageResponse<StockMovement> search(StockMovementSearchCriteria criteria) {
        List<Condition> conditions = new ArrayList<>();
        if (criteria.getMovementTypeId() != null) {
            conditions.add(STOCK_MOVEMENTS.MOVEMENT_TYPE_ID.eq(criteria.getMovementTypeId()));
        }
        if (criteria.getProductId() != null) {
            conditions.add(STOCK_MOVEMENTS.PRODUCT_ID.eq(criteria.getProductId()));
        }
        if (criteria.getLotId() != null) {
            conditions.add(STOCK_MOVEMENTS.LOT_ID.eq(criteria.getLotId()));
        }
        if (criteria.getWarehouseId() != null) {
            conditions.add(STOCK_MOVEMENTS.FROM_WAREHOUSE_ID.eq(criteria.getWarehouseId())
                    .or(STOCK_MOVEMENTS.TO_WAREHOUSE_ID.eq(criteria.getWarehouseId())));
        }
        if (criteria.getLocationId() != null) {
            conditions.add(STOCK_MOVEMENTS.FROM_LOCATION_ID.eq(criteria.getLocationId())
                    .or(STOCK_MOVEMENTS.TO_LOCATION_ID.eq(criteria.getLocationId())));
        }
        if (criteria.getReferenceNo() != null && !criteria.getReferenceNo().isBlank()) {
            conditions.add(STOCK_MOVEMENTS.REFERENCE_NO.containsIgnoreCase(criteria.getReferenceNo()));
        }

        long totalElements = ctx.fetchCount(STOCK_MOVEMENTS, conditions);

        int page = criteria.getPage();
        int size = criteria.getSize();
        int totalPages = size > 0 ? (int) Math.ceil((double) totalElements / size) : 0;

        List<StockMovement> items = ctx.selectFrom(STOCK_MOVEMENTS)
                .where(conditions)
                .orderBy(STOCK_MOVEMENTS.CREATED_AT.desc())
                .limit(size)
                .offset(page * size)
                .fetch()
                .map(mapper::toDomain);

        return PageResponse.<StockMovement>builder()
                .items(items)
                .totalElements(totalElements)
                .totalPages(totalPages)
                .pageNumber(page)
                .pageSize(size)
                .build();
    }
}
