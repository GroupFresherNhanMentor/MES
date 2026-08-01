package fpt.qn.mes.inventory.infrastructure.persistence;

import static fpt.qn.mes.jooq.Tables.PRODUCTS;
import static fpt.qn.mes.jooq.Tables.STOCK_ADJUSTMENT_APPROVALS;
import static fpt.qn.mes.jooq.Tables.USERS;
import static fpt.qn.mes.jooq.Tables.WAREHOUSE_LOCATIONS;
import static fpt.qn.mes.jooq.Tables.WAREHOUSES;

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
import fpt.qn.mes.inventory.domain.entities.StockAdjustmentApproval;
import fpt.qn.mes.inventory.domain.repository.StockAdjustmentApprovalRepository;
import fpt.qn.mes.inventory.domain.repository.criteria.StockAdjustmentApprovalSearchCriteria;
import fpt.qn.mes.jooq.tables.Users;
import fpt.qn.mes.jooq.tables.records.StockAdjustmentApprovalsRecord;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;

@Repository
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class StockAdjustmentApprovalPersistenceAdapter extends BaseRepository<StockAdjustmentApprovalsRecord>
        implements StockAdjustmentApprovalRepository {

    private static final Users CREATOR = USERS.as("creator");

    private static final Map<String, Field<?>> SORT_FIELDS = Map.of(
            "created_at", STOCK_ADJUSTMENT_APPROVALS.CREATED_AT);
    private static final Field<?> DEFAULT_SORT_FIELD = STOCK_ADJUSTMENT_APPROVALS.CREATED_AT;

    StockAdjustmentApprovalRecordMapper mapper;

    public StockAdjustmentApprovalPersistenceAdapter(DSLContext ctx, StockAdjustmentApprovalRecordMapper mapper) {
        super(ctx, STOCK_ADJUSTMENT_APPROVALS);
        this.mapper = mapper;
    }

    @Override
    public Optional<StockAdjustmentApproval> findById(UUID id) {
        return ctx.selectFrom(STOCK_ADJUSTMENT_APPROVALS)
                .where(STOCK_ADJUSTMENT_APPROVALS.ID.eq(id))
                .fetchOptional(r -> mapper.toDomain(r));
    }

    @Override
    public StockAdjustmentApproval save(StockAdjustmentApproval approval) {
        StockAdjustmentApprovalsRecord record = mapper.toRecord(approval);
        ctx.insertInto(STOCK_ADJUSTMENT_APPROVALS).set(record)
                .onConflict(STOCK_ADJUSTMENT_APPROVALS.ID).doUpdate().set(record)
                .execute();
        return approval;
    }

    @Override
    public PaginationResult<StockAdjustmentApproval> search(StockAdjustmentApprovalSearchCriteria criteria) {
        Condition condition = buildCondition(criteria);
        List<SortField<?>> orderBy = SortUtils.resolveSorts(criteria.getSort(), SORT_FIELDS, DEFAULT_SORT_FIELD);
        long total = ctx.fetchCount(STOCK_ADJUSTMENT_APPROVALS, condition);
        List<StockAdjustmentApproval> items = ctx.select()
                .from(STOCK_ADJUSTMENT_APPROVALS)
                .leftJoin(PRODUCTS).on(STOCK_ADJUSTMENT_APPROVALS.PRODUCT_ID.eq(PRODUCTS.ID))
                .leftJoin(WAREHOUSES).on(STOCK_ADJUSTMENT_APPROVALS.WAREHOUSE_ID.eq(WAREHOUSES.ID))
                .leftJoin(WAREHOUSE_LOCATIONS).on(STOCK_ADJUSTMENT_APPROVALS.LOCATION_ID.eq(WAREHOUSE_LOCATIONS.ID))
                .leftJoin(CREATOR).on(STOCK_ADJUSTMENT_APPROVALS.CREATED_BY.eq(CREATOR.ID))
                .where(condition)
                .orderBy(orderBy)
                .limit(criteria.getSize())
                .offset((long) criteria.getPage() * criteria.getSize())
                .fetch(r -> mapper.toDomain(
                        r.into(STOCK_ADJUSTMENT_APPROVALS), r.into(PRODUCTS),
                        r.into(WAREHOUSES), r.into(WAREHOUSE_LOCATIONS), r.into(CREATOR)));
        return PaginationResult.<StockAdjustmentApproval>builder().total(total).items(items).build();
    }

    @Override
    public void deleteById(UUID id) {
        ctx.deleteFrom(STOCK_ADJUSTMENT_APPROVALS)
                .where(STOCK_ADJUSTMENT_APPROVALS.ID.eq(id))
                .execute();
    }

    private Condition buildCondition(StockAdjustmentApprovalSearchCriteria criteria) {
        Condition condition = DSL.noCondition();
        if (criteria.getProductId() != null) {
            condition = condition.and(STOCK_ADJUSTMENT_APPROVALS.PRODUCT_ID.eq(criteria.getProductId()));
        }
        if (criteria.getWarehouseId() != null) {
            condition = condition.and(STOCK_ADJUSTMENT_APPROVALS.WAREHOUSE_ID.eq(criteria.getWarehouseId()));
        }
        if (criteria.getLocationId() != null) {
            condition = condition.and(STOCK_ADJUSTMENT_APPROVALS.LOCATION_ID.eq(criteria.getLocationId()));
        }
        if (criteria.getStockBalanceId() != null) {
            condition = condition.and(STOCK_ADJUSTMENT_APPROVALS.STOCK_BALANCE_ID.eq(criteria.getStockBalanceId()));
        }
        if (criteria.getCreatedBy() != null) {
            condition = condition.and(STOCK_ADJUSTMENT_APPROVALS.CREATED_BY.eq(criteria.getCreatedBy()));
        }
        return condition;
    }
}
