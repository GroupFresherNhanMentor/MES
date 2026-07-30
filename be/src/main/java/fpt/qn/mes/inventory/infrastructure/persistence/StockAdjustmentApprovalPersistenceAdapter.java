package fpt.qn.mes.inventory.infrastructure.persistence;

import static fpt.qn.mes.jooq.Tables.STOCK_ADJUSTMENT_APPROVALS;

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

import fpt.qn.mes.common.repository.BaseRepository;
import fpt.qn.mes.common.repository.SortUtils;
import fpt.qn.mes.common.util.UuidV7;
import fpt.qn.mes.inventory.domain.entities.StockAdjustmentApproval;
import fpt.qn.mes.inventory.domain.repository.StockAdjustmentApprovalRepository;
import fpt.qn.mes.inventory.domain.repository.criteria.StockAdjustmentApprovalSearchCriteria;
import fpt.qn.mes.jooq.tables.records.StockAdjustmentApprovalsRecord;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;

@Repository
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class StockAdjustmentApprovalPersistenceAdapter extends BaseRepository<StockAdjustmentApprovalsRecord>
        implements StockAdjustmentApprovalRepository {

    private static final Map<String, Field<?>> SORT_FIELDS = Map.of(
            "createdAt", STOCK_ADJUSTMENT_APPROVALS.CREATED_AT
    );

    private static final Field<?> DEFAULT_SORT_FIELD = STOCK_ADJUSTMENT_APPROVALS.CREATED_AT;

    StockAdjustmentApprovalRecordMapper mapper;

    public StockAdjustmentApprovalPersistenceAdapter(DSLContext ctx, StockAdjustmentApprovalRecordMapper mapper) {
        super(ctx, STOCK_ADJUSTMENT_APPROVALS);
        this.mapper = mapper;
    }

    @Override
    public Optional<StockAdjustmentApproval> findById(UUID id) {
        StockAdjustmentApprovalsRecord record = ctx.selectFrom(STOCK_ADJUSTMENT_APPROVALS)
                .where(STOCK_ADJUSTMENT_APPROVALS.ID.eq(id))
                .fetchOne();
        return Optional.ofNullable(mapper.toDomain(record));
    }

    @Override
    public StockAdjustmentApproval save(StockAdjustmentApproval approval) {
        StockAdjustmentApprovalsRecord record = mapper.toRecord(approval);
        if (record.getId() == null) {
            record.setId(UuidV7.generate());
        }
        ctx.attach(record);
        record.store();
        return mapper.toDomain(record);
    }

    @Override
    public List<StockAdjustmentApproval> search(StockAdjustmentApprovalSearchCriteria criteria) {
        Condition condition = buildCondition(criteria);
        int page = criteria.getPage();
        int size = criteria.getSize();

        List<SortField<?>> orderBy = SortUtils.resolveSorts(criteria.getSort(), SORT_FIELDS, DEFAULT_SORT_FIELD);

        return ctx.selectFrom(STOCK_ADJUSTMENT_APPROVALS)
                .where(condition)
                .orderBy(orderBy)
                .limit(size)
                .offset(page * size)
                .fetch()
                .map(mapper::toDomain);
    }

    @Override
    public long count(StockAdjustmentApprovalSearchCriteria criteria) {
        return count(buildCondition(criteria));
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
