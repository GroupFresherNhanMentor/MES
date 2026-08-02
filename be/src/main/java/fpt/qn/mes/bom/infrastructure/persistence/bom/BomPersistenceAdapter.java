package fpt.qn.mes.bom.infrastructure.persistence.bom;

import static fpt.qn.mes.jooq.Tables.BOM_ITEMS;
import static fpt.qn.mes.jooq.Tables.BOM_STATUSES;
import static fpt.qn.mes.jooq.Tables.BOMS;
import static fpt.qn.mes.jooq.Tables.PRODUCTS;
import static fpt.qn.mes.jooq.Tables.UNITS_OF_MEASURE;
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

import fpt.qn.mes.bom.domain.entities.Bom;
import fpt.qn.mes.bom.domain.entities.BomItem;
import fpt.qn.mes.bom.domain.repository.BomRepository;
import fpt.qn.mes.bom.domain.repository.criteria.BomSearchCriteria;
import fpt.qn.mes.bom.infrastructure.persistence.bomitem.BomItemRecordMapper;
import fpt.qn.mes.common.domainQuery.PaginationResult;
import fpt.qn.mes.common.repository.BaseRepository;
import fpt.qn.mes.common.repository.SortUtils;
import fpt.qn.mes.jooq.tables.Users;
import fpt.qn.mes.jooq.tables.records.BomsRecord;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;

@Repository
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class BomPersistenceAdapter extends BaseRepository<BomsRecord> implements BomRepository {

    private static final Users CREATOR = USERS.as("creator");

    private static final Map<String, Field<?>> SORT_FIELDS = Map.of(
            "version",    BOMS.VERSION,
            "created_at", BOMS.CREATED_AT);
    private static final Field<?> DEFAULT_SORT_FIELD = BOMS.CREATED_AT;

    BomRecordMapper mapper;
    BomItemRecordMapper itemMapper;

    public BomPersistenceAdapter(DSLContext ctx, BomRecordMapper mapper, BomItemRecordMapper itemMapper) {
        super(ctx, BOMS);
        this.mapper = mapper;
        this.itemMapper = itemMapper;
    }

    @Override
    public Optional<Bom> findById(UUID id) {
        return ctx.select()
                .from(BOMS)
                .leftJoin(PRODUCTS).on(BOMS.FINISHED_PRODUCT_ID.eq(PRODUCTS.ID))
                .leftJoin(BOM_STATUSES).on(BOMS.BOM_STATUS_ID.eq(BOM_STATUSES.ID))
                .leftJoin(CREATOR).on(BOMS.CREATED_BY.eq(CREATOR.ID))
                .where(BOMS.ID.eq(id))
                .fetchOptional(r -> {
                    List<BomItem> items = ctx.select()
                            .from(BOM_ITEMS)
                            .leftJoin(PRODUCTS).on(BOM_ITEMS.MATERIAL_PRODUCT_ID.eq(PRODUCTS.ID))
                            .leftJoin(UNITS_OF_MEASURE).on(PRODUCTS.UNIT_ID.eq(UNITS_OF_MEASURE.ID))
                            .where(BOM_ITEMS.BOM_ID.eq(id))
                            .fetch(ir -> itemMapper.toDomain(ir.into(BOM_ITEMS), ir.into(PRODUCTS), ir.into(UNITS_OF_MEASURE)));
                    return mapper.toDomain(r.into(BOMS), r.into(PRODUCTS), r.into(BOM_STATUSES), r.into(CREATOR), items);
                });
    }

    @Override
    public Optional<Bom> findActiveByFinishedProductId(UUID finishedProductId) {
        return ctx.select()
                .from(BOMS)
                .leftJoin(PRODUCTS).on(BOMS.FINISHED_PRODUCT_ID.eq(PRODUCTS.ID))
                .leftJoin(BOM_STATUSES).on(BOMS.BOM_STATUS_ID.eq(BOM_STATUSES.ID))
                .leftJoin(CREATOR).on(BOMS.CREATED_BY.eq(CREATOR.ID))
                .where(BOMS.FINISHED_PRODUCT_ID.eq(finishedProductId))
                .and(BOM_STATUSES.NAME.eq("ACTIVE"))
                .fetchOptional(r -> mapper.toDomain(r.into(BOMS), r.into(PRODUCTS), r.into(BOM_STATUSES), r.into(CREATOR), List.of()));
    }

    @Override
    public Bom save(Bom bom) {
        BomsRecord r = mapper.toRecord(bom);
        ctx.insertInto(BOMS).set(r).onConflict(BOMS.ID).doUpdate().set(r).execute();
        return bom;
    }

    @Override
    public Bom update(Bom bom) {
        BomsRecord r = mapper.toRecord(bom);
        ctx.update(BOMS).set(r).where(BOMS.ID.eq(bom.getId())).execute();
        return bom;
    }

    @Override
    public PaginationResult<Bom> search(BomSearchCriteria criteria) {
        Condition condition = buildCondition(criteria);
        List<SortField<?>> orderBy = SortUtils.resolveSorts(criteria.getSort(), SORT_FIELDS, DEFAULT_SORT_FIELD);
        long total = ctx.fetchCount(BOMS, condition);
        List<Bom> items = ctx.select()
                .from(BOMS)
                .leftJoin(PRODUCTS).on(BOMS.FINISHED_PRODUCT_ID.eq(PRODUCTS.ID))
                .leftJoin(BOM_STATUSES).on(BOMS.BOM_STATUS_ID.eq(BOM_STATUSES.ID))
                .leftJoin(CREATOR).on(BOMS.CREATED_BY.eq(CREATOR.ID))
                .where(condition)
                .orderBy(orderBy)
                .limit(criteria.getSize())
                .offset((long) criteria.getPage() * criteria.getSize())
                .fetch(r -> mapper.toDomain(r.into(BOMS), r.into(PRODUCTS), r.into(BOM_STATUSES), r.into(CREATOR), List.of()));
        return PaginationResult.<Bom>builder().total(total).items(items).build();
    }

    @Override
    public boolean existsByFinishedProductIdAndVersion(UUID finishedProductId, Integer version) {
        return ctx.fetchExists(BOMS,
                BOMS.FINISHED_PRODUCT_ID.eq(finishedProductId).and(BOMS.VERSION.eq(version)));
    }

    @Override
    public void deactivateActiveBomsForProduct(UUID finishedProductId, UUID activeStatusId, UUID inactiveStatusId) {
        ctx.update(BOMS)
                .set(BOMS.BOM_STATUS_ID, inactiveStatusId)
                .where(BOMS.FINISHED_PRODUCT_ID.eq(finishedProductId))
                .and(BOMS.BOM_STATUS_ID.eq(activeStatusId))
                .execute();
    }

    @Override
    public int findMaxVersionByFinishedProductId(UUID finishedProductId) {
        Integer max = ctx.select(DSL.max(BOMS.VERSION))
                .from(BOMS)
                .where(BOMS.FINISHED_PRODUCT_ID.eq(finishedProductId))
                .fetchOneInto(Integer.class);
        return max != null ? max : 0;
    }

    private Condition buildCondition(BomSearchCriteria criteria) {
        Condition condition = DSL.noCondition();
        if (criteria.getFinishedProductId() != null) {
            condition = condition.and(BOMS.FINISHED_PRODUCT_ID.eq(criteria.getFinishedProductId()));
        }
        if (criteria.getBomStatusId() != null) {
            condition = condition.and(BOMS.BOM_STATUS_ID.eq(criteria.getBomStatusId()));
        }
        return condition;
    }
}
