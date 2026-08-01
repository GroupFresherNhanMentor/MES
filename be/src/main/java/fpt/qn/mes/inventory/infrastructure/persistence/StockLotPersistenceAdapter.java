package fpt.qn.mes.inventory.infrastructure.persistence;

import static fpt.qn.mes.jooq.Tables.LOT_TYPES;
import static fpt.qn.mes.jooq.Tables.PRODUCTS;
import static fpt.qn.mes.jooq.Tables.STOCK_LOTS;
import static fpt.qn.mes.jooq.Tables.USERS;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.SortField;
import org.jooq.impl.DSL;
import org.springframework.stereotype.Repository;

import fpt.qn.mes.common.domainQuery.PaginationResult;
import fpt.qn.mes.common.repository.BaseRepository;
import fpt.qn.mes.common.repository.SortUtils;
import fpt.qn.mes.inventory.domain.entities.StockLot;
import fpt.qn.mes.inventory.domain.repository.StockLotRepository;
import fpt.qn.mes.inventory.domain.repository.criteria.StockLotSearchCriteria;
import fpt.qn.mes.jooq.tables.Users;
import fpt.qn.mes.jooq.tables.records.StockLotsRecord;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;

@Repository
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class StockLotPersistenceAdapter extends BaseRepository<StockLotsRecord> implements StockLotRepository {

    private static final Users CREATOR = USERS.as("creator");
    private static final Users UPDATER = USERS.as("updater");

    private static final Map<String, Field<?>> SORT_FIELDS = Map.of(
            "createdAt", STOCK_LOTS.CREATED_AT,
            "lotNumber", STOCK_LOTS.LOT_NUMBER,
            "expiryDate", STOCK_LOTS.EXPIRY_DATE
    );
    private static final Field<?> DEFAULT_SORT_FIELD = STOCK_LOTS.CREATED_AT;

    StockLotRecordMapper mapper;

    public StockLotPersistenceAdapter(DSLContext ctx, StockLotRecordMapper mapper) {
        super(ctx, STOCK_LOTS);
        this.mapper = mapper;
    }

    @Override
    public Optional<StockLot> findById(UUID id) {
        return ctx.select()
                .from(STOCK_LOTS)
                .leftJoin(PRODUCTS).on(PRODUCTS.ID.eq(STOCK_LOTS.PRODUCT_ID))
                .leftJoin(LOT_TYPES).on(LOT_TYPES.ID.eq(STOCK_LOTS.LOT_TYPE_ID))
                .leftJoin(CREATOR).on(CREATOR.ID.eq(STOCK_LOTS.CREATED_BY))
                .leftJoin(UPDATER).on(UPDATER.ID.eq(STOCK_LOTS.UPDATED_BY))
                .where(STOCK_LOTS.ID.eq(id))
                .fetchOptional(r -> mapper.toDomain(r.into(STOCK_LOTS), r.into(PRODUCTS), r.into(LOT_TYPES),
                        r.into(CREATOR), r.into(UPDATER)));
    }

    @Override
    public Optional<StockLot> findByLotNumber(String lotNumber) {
        if (lotNumber == null || lotNumber.isBlank()) return Optional.empty();
        return ctx.select()
                .from(STOCK_LOTS)
                .leftJoin(PRODUCTS).on(PRODUCTS.ID.eq(STOCK_LOTS.PRODUCT_ID))
                .leftJoin(LOT_TYPES).on(LOT_TYPES.ID.eq(STOCK_LOTS.LOT_TYPE_ID))
                .leftJoin(CREATOR).on(CREATOR.ID.eq(STOCK_LOTS.CREATED_BY))
                .leftJoin(UPDATER).on(UPDATER.ID.eq(STOCK_LOTS.UPDATED_BY))
                .where(STOCK_LOTS.LOT_NUMBER.eq(lotNumber))
                .fetchOptional(r -> mapper.toDomain(r.into(STOCK_LOTS), r.into(PRODUCTS), r.into(LOT_TYPES),
                        r.into(CREATOR), r.into(UPDATER)));
    }

    @Override
    public boolean existsByLotNumber(String lotNumber) {
        return ctx.fetchExists(STOCK_LOTS, STOCK_LOTS.LOT_NUMBER.eq(lotNumber));
    }

    @Override
    public StockLot save(StockLot lot) {
        StockLotsRecord r = mapper.toRecord(lot);
        ctx.insertInto(STOCK_LOTS).set(r)
                .onConflict(STOCK_LOTS.ID).doUpdate().set(r)
                .execute();
        return lot;
    }

    @Override
    public PaginationResult<StockLot> search(StockLotSearchCriteria criteria) {
        Condition condition = buildCondition(criteria);
        List<SortField<?>> orderBy = SortUtils.resolveSorts(criteria.getSort(), SORT_FIELDS, DEFAULT_SORT_FIELD);

        long total = ctx.fetchCount(STOCK_LOTS, condition);
        List<StockLot> items = ctx.select()
                .from(STOCK_LOTS)
                .leftJoin(PRODUCTS).on(PRODUCTS.ID.eq(STOCK_LOTS.PRODUCT_ID))
                .leftJoin(LOT_TYPES).on(LOT_TYPES.ID.eq(STOCK_LOTS.LOT_TYPE_ID))
                .leftJoin(CREATOR).on(CREATOR.ID.eq(STOCK_LOTS.CREATED_BY))
                .leftJoin(UPDATER).on(UPDATER.ID.eq(STOCK_LOTS.UPDATED_BY))
                .where(condition)
                .orderBy(orderBy)
                .limit(criteria.getSize())
                .offset((long) criteria.getPage() * criteria.getSize())
                .fetch(r -> mapper.toDomain(r.into(STOCK_LOTS), r.into(PRODUCTS), r.into(LOT_TYPES),
                        r.into(CREATOR), r.into(UPDATER)));

        return PaginationResult.<StockLot>builder().total(total).items(items).build();
    }

    private Condition buildCondition(StockLotSearchCriteria criteria) {
        Condition condition = DSL.noCondition();
        if (criteria.getProductId() != null) {
            condition = condition.and(STOCK_LOTS.PRODUCT_ID.eq(criteria.getProductId()));
        }
        if (criteria.getLotTypeId() != null) {
            condition = condition.and(STOCK_LOTS.LOT_TYPE_ID.eq(criteria.getLotTypeId()));
        }
        if (criteria.getLotNumber() != null && !criteria.getLotNumber().isBlank()) {
            condition = condition.and(STOCK_LOTS.LOT_NUMBER.containsIgnoreCase(criteria.getLotNumber()));
        }
        return condition;
    }
}
