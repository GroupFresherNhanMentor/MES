package fpt.qn.mes.quality.infrastructure.persistence;

import static fpt.qn.mes.jooq.Tables.DEFECT_TYPES;
import static fpt.qn.mes.jooq.Tables.LOT_TYPES;
import static fpt.qn.mes.jooq.Tables.PRODUCTS;
import static fpt.qn.mes.jooq.Tables.QC_ACTIONS;
import static fpt.qn.mes.jooq.Tables.QC_STATUSES;
import static fpt.qn.mes.jooq.Tables.QUALITY_INSPECTIONS;
import static fpt.qn.mes.jooq.Tables.QUALITY_INSPECTION_RESULTS;
import static fpt.qn.mes.jooq.Tables.STOCK_LOTS;
import static fpt.qn.mes.jooq.Tables.USERS;
import static fpt.qn.mes.jooq.Tables.WORK_ORDERS;

import java.math.BigDecimal;
import java.util.ArrayList;
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
import fpt.qn.mes.jooq.tables.records.QualityInspectionsRecord;
import fpt.qn.mes.quality.domain.entities.QualityInspection;
import fpt.qn.mes.quality.domain.entities.QualityInspectionResult;
import fpt.qn.mes.quality.domain.repository.QualityInspectionRepository;
import fpt.qn.mes.quality.domain.repository.criteria.QualityInspectionResultSearchCriteria;
import fpt.qn.mes.quality.domain.repository.criteria.QualityInspectionSearchCriteria;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;

@Repository
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class QualityPersistenceAdapter extends BaseRepository<QualityInspectionsRecord> implements QualityInspectionRepository {

    private static final Map<String, Field<?>> SORT_FIELDS = Map.of(
        "createdAt", QUALITY_INSPECTIONS.CREATED_AT,
        "quantity",  QUALITY_INSPECTIONS.QUANTITY
    );
    private static final Field<?> DEFAULT_SORT_FIELD = QUALITY_INSPECTIONS.CREATED_AT;

    private static final Map<String, Field<?>> RESULT_SORT_FIELDS = Map.of(
        "inspectedAt", QUALITY_INSPECTION_RESULTS.INSPECTED_AT,
        "quantity",    QUALITY_INSPECTION_RESULTS.QUANTITY
    );
    private static final Field<?> DEFAULT_RESULT_SORT_FIELD = QUALITY_INSPECTION_RESULTS.INSPECTED_AT;

    QualityRecordMapper mapper;
    DSLContext ctx;

    public QualityPersistenceAdapter(DSLContext ctx, QualityRecordMapper mapper) {
        super(ctx, QUALITY_INSPECTIONS);
        this.mapper = mapper;
        this.ctx = ctx;
    }

    @Override
    public Optional<QualityInspection> findById(UUID id) {
        return ctx.select()
            .from(QUALITY_INSPECTIONS)
            .join(QC_STATUSES).on(QC_STATUSES.ID.eq(QUALITY_INSPECTIONS.QC_STATUS_ID))
            .join(PRODUCTS).on(PRODUCTS.ID.eq(QUALITY_INSPECTIONS.PRODUCT_ID))
            .join(WORK_ORDERS).on(WORK_ORDERS.ID.eq(QUALITY_INSPECTIONS.WORK_ORDER_ID))
            .join(STOCK_LOTS).on(STOCK_LOTS.ID.eq(QUALITY_INSPECTIONS.LOT_ID))
            .leftJoin(LOT_TYPES).on(LOT_TYPES.ID.eq(STOCK_LOTS.LOT_TYPE_ID))
            .where(QUALITY_INSPECTIONS.ID.eq(id))
            .fetchOptional(r -> mapper.toDomain(
                r.into(QUALITY_INSPECTIONS), r.into(QC_STATUSES),
                r.into(WORK_ORDERS), r.into(PRODUCTS),
                r.into(STOCK_LOTS), r.into(LOT_TYPES)));
    }

    @Override
    public QualityInspection save(QualityInspection inspection) {
        QualityInspectionsRecord record = mapper.toRecord(inspection);
        ctx.insertInto(QUALITY_INSPECTIONS)
            .set(record)
            .onConflict(QUALITY_INSPECTIONS.ID)
            .doUpdate()
            .set(record)
            .execute();
        return inspection;
    }

    @Override
    public PaginationResult<QualityInspection> search(QualityInspectionSearchCriteria criteria) {
        Condition condition = buildCondition(criteria);
        List<SortField<?>> orderBy = SortUtils.resolveSorts(criteria.getSort(), SORT_FIELDS, DEFAULT_SORT_FIELD);

        long total = ctx.selectCount()
            .from(QUALITY_INSPECTIONS)
            .join(PRODUCTS).on(PRODUCTS.ID.eq(QUALITY_INSPECTIONS.PRODUCT_ID))
            .join(WORK_ORDERS).on(WORK_ORDERS.ID.eq(QUALITY_INSPECTIONS.WORK_ORDER_ID))
            .join(STOCK_LOTS).on(STOCK_LOTS.ID.eq(QUALITY_INSPECTIONS.LOT_ID))
            .leftJoin(LOT_TYPES).on(LOT_TYPES.ID.eq(STOCK_LOTS.LOT_TYPE_ID))
            .where(condition)
            .fetchOne(0, Long.class);

        List<QualityInspection> items = ctx.select()
            .from(QUALITY_INSPECTIONS)
            .join(QC_STATUSES).on(QC_STATUSES.ID.eq(QUALITY_INSPECTIONS.QC_STATUS_ID))
            .join(PRODUCTS).on(PRODUCTS.ID.eq(QUALITY_INSPECTIONS.PRODUCT_ID))
            .join(WORK_ORDERS).on(WORK_ORDERS.ID.eq(QUALITY_INSPECTIONS.WORK_ORDER_ID))
            .join(STOCK_LOTS).on(STOCK_LOTS.ID.eq(QUALITY_INSPECTIONS.LOT_ID))
            .leftJoin(LOT_TYPES).on(LOT_TYPES.ID.eq(STOCK_LOTS.LOT_TYPE_ID))
            .where(condition)
            .orderBy(orderBy)
            .limit(criteria.getSize())
            .offset(criteria.getPage() * criteria.getSize())
            .fetch(r -> mapper.toDomain(
                r.into(QUALITY_INSPECTIONS), r.into(QC_STATUSES),
                r.into(WORK_ORDERS), r.into(PRODUCTS),
                r.into(STOCK_LOTS), r.into(LOT_TYPES)));

        return PaginationResult.<QualityInspection>builder().total(total).items(items).build();
    }

    @Override
    public QualityInspectionResult saveResult(QualityInspectionResult result) {
        var record = mapper.toRecord(result);
        ctx.insertInto(QUALITY_INSPECTION_RESULTS).set(record).execute();
        return result;
    }

    @Override
    public PaginationResult<QualityInspectionResult> searchResults(QualityInspectionResultSearchCriteria criteria) {
        Condition condition = buildResultCondition(criteria);
        List<SortField<?>> orderBy = SortUtils.resolveSorts(criteria.getSort(), RESULT_SORT_FIELDS, DEFAULT_RESULT_SORT_FIELD);

        long total = ctx.selectCount()
            .from(QUALITY_INSPECTION_RESULTS)
            .where(condition)
            .fetchOne(0, Long.class);

        List<QualityInspectionResult> items = ctx.select()
            .from(QUALITY_INSPECTION_RESULTS)
            .leftJoin(DEFECT_TYPES).on(DEFECT_TYPES.ID.eq(QUALITY_INSPECTION_RESULTS.DEFECT_TYPE_ID))
            .leftJoin(QC_ACTIONS).on(QC_ACTIONS.ID.eq(QUALITY_INSPECTION_RESULTS.ACTION_ID))
            .leftJoin(USERS).on(USERS.ID.eq(QUALITY_INSPECTION_RESULTS.INSPECTOR_ID))
            .where(condition)
            .orderBy(orderBy)
            .limit(criteria.getSize())
            .offset(criteria.getPage() * criteria.getSize())
            .fetch(r -> mapper.toDomain(
                r.into(QUALITY_INSPECTION_RESULTS), r.into(DEFECT_TYPES),
                r.into(QC_ACTIONS), r.into(USERS)));

        return PaginationResult.<QualityInspectionResult>builder().total(total).items(items).build();
    }

    private Condition buildResultCondition(QualityInspectionResultSearchCriteria criteria) {
        List<Condition> conditions = new ArrayList<>();
        conditions.add(QUALITY_INSPECTION_RESULTS.INSPECTION_ID.eq(criteria.getInspectionId()));
        if (criteria.getIsPass() != null) {
            conditions.add(QUALITY_INSPECTION_RESULTS.IS_PASS.eq(criteria.getIsPass()));
        }
        if (criteria.getDefectTypeId() != null) {
            conditions.add(QUALITY_INSPECTION_RESULTS.DEFECT_TYPE_ID.eq(criteria.getDefectTypeId()));
        }
        if (criteria.getInspectorId() != null) {
            conditions.add(QUALITY_INSPECTION_RESULTS.INSPECTOR_ID.eq(criteria.getInspectorId()));
        }
        if (criteria.getActionId() != null) {
            conditions.add(QUALITY_INSPECTION_RESULTS.ACTION_ID.eq(criteria.getActionId()));
        }
        return DSL.and(conditions);
    }

    @Override
    public BigDecimal sumResultQuantities(UUID inspectionId) {
        BigDecimal sum = ctx.select(DSL.sum(QUALITY_INSPECTION_RESULTS.QUANTITY))
            .from(QUALITY_INSPECTION_RESULTS)
            .where(QUALITY_INSPECTION_RESULTS.INSPECTION_ID.eq(inspectionId))
            .fetchOneInto(BigDecimal.class);
        return sum != null ? sum : BigDecimal.ZERO;
    }

    @Override
    public void decrementRemainingQuantity(UUID inspectionId, BigDecimal amount) {
        ctx.update(QUALITY_INSPECTIONS)
            .set(QUALITY_INSPECTIONS.REMAINING_QUANTITY, QUALITY_INSPECTIONS.REMAINING_QUANTITY.subtract(amount))
            .where(QUALITY_INSPECTIONS.ID.eq(inspectionId))
            .execute();
    }

    @Override
    public void updateStatus(UUID inspectionId, UUID newStatusId) {
        ctx.update(QUALITY_INSPECTIONS)
            .set(QUALITY_INSPECTIONS.QC_STATUS_ID, newStatusId)
            .where(QUALITY_INSPECTIONS.ID.eq(inspectionId))
            .execute();
    }

    private Condition buildCondition(QualityInspectionSearchCriteria criteria) {
        List<Condition> conditions = new ArrayList<>();
        if (criteria.getProductCode() != null && !criteria.getProductCode().isBlank()) {
            conditions.add(PRODUCTS.CODE.likeIgnoreCase("%" + criteria.getProductCode() + "%"));
        }
        if (criteria.getProductName() != null && !criteria.getProductName().isBlank()) {
            conditions.add(PRODUCTS.NAME.likeIgnoreCase("%" + criteria.getProductName() + "%"));
        }
        if (criteria.getProductTypeId() != null) {
            conditions.add(PRODUCTS.PRODUCT_TYPE_ID.eq(criteria.getProductTypeId()));
        }
        if (criteria.getWorkOrderCode() != null && !criteria.getWorkOrderCode().isBlank()) {
            conditions.add(WORK_ORDERS.CODE.likeIgnoreCase("%" + criteria.getWorkOrderCode() + "%"));
        }
        if (criteria.getLotNumber() != null && !criteria.getLotNumber().isBlank()) {
            conditions.add(STOCK_LOTS.LOT_NUMBER.likeIgnoreCase("%" + criteria.getLotNumber() + "%"));
        }
        if (criteria.getLotType() != null && !criteria.getLotType().isBlank()) {
            conditions.add(LOT_TYPES.NAME.likeIgnoreCase("%" + criteria.getLotType() + "%"));
        }
        if (criteria.getQcStatusId() != null) {
            conditions.add(QUALITY_INSPECTIONS.QC_STATUS_ID.eq(criteria.getQcStatusId()));
        }
        return conditions.isEmpty() ? DSL.noCondition() : DSL.and(conditions);
    }
}
