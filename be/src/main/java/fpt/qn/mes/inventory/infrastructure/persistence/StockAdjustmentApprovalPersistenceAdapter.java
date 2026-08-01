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
import org.jooq.Record;
import org.jooq.SortField;
import org.jooq.impl.DSL;
import org.springframework.stereotype.Repository;

import fpt.qn.mes.common.repository.BaseRepository;
import fpt.qn.mes.common.repository.SortUtils;
import fpt.qn.mes.inventory.domain.entities.StockAdjustmentApproval;
import fpt.qn.mes.inventory.domain.repository.StockAdjustmentApprovalRepository;
import fpt.qn.mes.inventory.domain.repository.criteria.StockAdjustmentApprovalSearchCriteria;
import fpt.qn.mes.jooq.tables.Users;
import fpt.qn.mes.jooq.tables.records.ProductsRecord;
import fpt.qn.mes.jooq.tables.records.StockAdjustmentApprovalsRecord;
import fpt.qn.mes.jooq.tables.records.UsersRecord;
import fpt.qn.mes.jooq.tables.records.WarehouseLocationsRecord;
import fpt.qn.mes.jooq.tables.records.WarehousesRecord;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;

@Repository
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class StockAdjustmentApprovalPersistenceAdapter extends BaseRepository<StockAdjustmentApprovalsRecord>
        implements StockAdjustmentApprovalRepository {

    private static final Users CREATOR = USERS.as("creator");

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
        ctx.insertInto(STOCK_ADJUSTMENT_APPROVALS).set(record)
                .onConflict(STOCK_ADJUSTMENT_APPROVALS.ID).doUpdate().set(record)
                .execute();
        return approval;
    }

    @Override
    public List<StockAdjustmentApproval> search(StockAdjustmentApprovalSearchCriteria criteria) {
        Condition condition = buildCondition(criteria);
        List<SortField<?>> orderBy = SortUtils.resolveSorts(criteria.getSort(), SORT_FIELDS, DEFAULT_SORT_FIELD);
        return ctx.select()
                .from(STOCK_ADJUSTMENT_APPROVALS)
                .leftJoin(PRODUCTS).on(STOCK_ADJUSTMENT_APPROVALS.PRODUCT_ID.eq(PRODUCTS.ID))
                .leftJoin(WAREHOUSES).on(STOCK_ADJUSTMENT_APPROVALS.WAREHOUSE_ID.eq(WAREHOUSES.ID))
                .leftJoin(WAREHOUSE_LOCATIONS).on(STOCK_ADJUSTMENT_APPROVALS.LOCATION_ID.eq(WAREHOUSE_LOCATIONS.ID))
                .leftJoin(CREATOR).on(STOCK_ADJUSTMENT_APPROVALS.CREATED_BY.eq(CREATOR.ID))
                .where(condition)
                .orderBy(orderBy)
                .limit(criteria.getSize())
                .offset((long) criteria.getPage() * criteria.getSize())
                .fetch(r -> mapRecord(r));
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

    private StockAdjustmentApproval mapRecord(Record r) {
        StockAdjustmentApprovalsRecord ap = r.into(STOCK_ADJUSTMENT_APPROVALS);
        ProductsRecord product = r.into(PRODUCTS);
        WarehousesRecord warehouse = r.into(WAREHOUSES);
        WarehouseLocationsRecord location = r.into(WAREHOUSE_LOCATIONS);
        UsersRecord creator = r.into(CREATOR);

        StockAdjustmentApproval.ProductRef productRef = product.getId() != null
                ? StockAdjustmentApproval.ProductRef.builder()
                        .id(product.getId()).code(product.getCode()).name(product.getName()).build()
                : null;

        StockAdjustmentApproval.WarehouseRef warehouseRef = warehouse.getId() != null
                ? StockAdjustmentApproval.WarehouseRef.builder()
                        .id(warehouse.getId()).code(warehouse.getCode()).name(warehouse.getName()).build()
                : null;

        StockAdjustmentApproval.WarehouseLocationRef locationRef = location.getId() != null
                ? StockAdjustmentApproval.WarehouseLocationRef.builder()
                        .id(location.getId()).code(location.getCode()).name(location.getName()).build()
                : null;

        StockAdjustmentApproval.UserRef createdByUser = creator.getId() != null
                ? StockAdjustmentApproval.UserRef.builder()
                        .id(creator.getId())
                        .username(creator.getUsername())
                        .fullName(creator.getFullName())
                        .build()
                : null;

        return StockAdjustmentApproval.builder()
                .id(ap.getId())
                .productId(ap.getProductId())
                .warehouseId(ap.getWarehouseId())
                .locationId(ap.getLocationId())
                .stockBalanceId(ap.getStockBalanceId())
                .quantityAdjustment(ap.getQuantityAdjustment())
                .reason(ap.getReason())
                .referenceNo(ap.getReferenceNo())
                .createdBy(ap.getCreatedBy())
                .createdAt(ap.getCreatedAt() != null ? ap.getCreatedAt().toInstant() : null)
                .product(productRef)
                .warehouse(warehouseRef)
                .location(locationRef)
                .createdByUser(createdByUser)
                .build();
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
