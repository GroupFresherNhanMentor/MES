package fpt.qn.mes.inventory.infrastructure.persistence;

import static fpt.qn.mes.jooq.Tables.STOCK_LOTS;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.jooq.Condition;
import org.jooq.DSLContext;
import org.springframework.stereotype.Repository;

import fpt.qn.mes.common.dto.response.PageResponse;
import fpt.qn.mes.common.repository.BaseRepository;
import fpt.qn.mes.inventory.domain.entities.StockLot;
import fpt.qn.mes.inventory.domain.repository.StockLotRepository;
import fpt.qn.mes.inventory.domain.repository.criteria.StockLotSearchCriteria;
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
    public PageResponse<StockLot> search(StockLotSearchCriteria criteria) {
        List<Condition> conditions = new ArrayList<>();
        if (criteria.getProductId() != null) {
            conditions.add(STOCK_LOTS.PRODUCT_ID.eq(criteria.getProductId()));
        }
        if (criteria.getLotTypeId() != null) {
            conditions.add(STOCK_LOTS.LOT_TYPE_ID.eq(criteria.getLotTypeId()));
        }
        if (criteria.getLotNumber() != null && !criteria.getLotNumber().isBlank()) {
            conditions.add(STOCK_LOTS.LOT_NUMBER.containsIgnoreCase(criteria.getLotNumber()));
        }
        if (criteria.getExpiryBefore() != null) {
            conditions.add(STOCK_LOTS.EXPIRY_DATE.lessOrEqual(criteria.getExpiryBefore()));
        }

        long totalElements = ctx.fetchCount(STOCK_LOTS, conditions);

        int page = criteria.getPage();
        int size = criteria.getSize();
        int totalPages = size > 0 ? (int) Math.ceil((double) totalElements / size) : 0;

        List<StockLot> items = ctx.selectFrom(STOCK_LOTS)
                .where(conditions)
                .orderBy(STOCK_LOTS.CREATED_AT.desc())
                .limit(size)
                .offset(page * size)
                .fetch()
                .map(mapper::toDomain);

        return PageResponse.<StockLot>builder()
                .items(items)
                .totalElements(totalElements)
                .totalPages(totalPages)
                .pageNumber(page)
                .pageSize(size)
                .build();
    }
}
