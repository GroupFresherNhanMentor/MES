package fpt.qn.mes.bom.infrastructure.persistence;

import static fpt.qn.mes.jooq.Tables.BOMS;
import static fpt.qn.mes.jooq.Tables.BOM_ITEMS;
import static fpt.qn.mes.jooq.Tables.BOM_STATUSES;
import static fpt.qn.mes.jooq.Tables.PRODUCTS;
import static fpt.qn.mes.jooq.Tables.UNITS_OF_MEASURE;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.impl.DSL;
import org.springframework.stereotype.Repository;

import fpt.qn.mes.bom.domain.entities.Bom;
import fpt.qn.mes.bom.domain.entities.BomItem;
import fpt.qn.mes.bom.domain.repository.BomRepository;
import fpt.qn.mes.common.domainQuery.PaginationResult;
import fpt.qn.mes.common.repository.BaseRepository;
import fpt.qn.mes.jooq.tables.records.BomItemsRecord;
import fpt.qn.mes.jooq.tables.records.BomStatusesRecord;
import fpt.qn.mes.jooq.tables.records.BomsRecord;
import fpt.qn.mes.jooq.tables.records.ProductsRecord;
import fpt.qn.mes.jooq.tables.records.UnitsOfMeasureRecord;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;

@Repository
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class BomPersistenceAdapter extends BaseRepository<BomsRecord> implements BomRepository {

    BomRecordMapper mapper;
    DSLContext dslCtx;

    public BomPersistenceAdapter(DSLContext ctx, BomRecordMapper mapper) {
        super(ctx, BOMS);
        this.mapper = mapper;
        this.dslCtx = ctx;
    }

    private List<BomItem> fetchItemsForBom(UUID bomId) {
        return dslCtx.select(BOM_ITEMS.fields())
                .select(PRODUCTS.fields())
                .select(UNITS_OF_MEASURE.fields())
                .from(BOM_ITEMS)
                .leftJoin(PRODUCTS).on(BOM_ITEMS.MATERIAL_PRODUCT_ID.eq(PRODUCTS.ID))
                .leftJoin(UNITS_OF_MEASURE).on(BOM_ITEMS.UNIT_ID.eq(UNITS_OF_MEASURE.ID))
                .where(BOM_ITEMS.BOM_ID.eq(bomId))
                .fetch(r -> {
                    BomItemsRecord itemRec = r.into(BOM_ITEMS);
                    ProductsRecord matRec = r.into(PRODUCTS);
                    UnitsOfMeasureRecord unitRec = r.into(UNITS_OF_MEASURE);
                    return mapper.toDomain(itemRec, matRec, unitRec);
                });
    }

    @Override
    public Optional<Bom> findById(UUID id) {
        Record record = dslCtx.select(BOMS.fields())
                .select(PRODUCTS.fields())
                .select(BOM_STATUSES.fields())
                .from(BOMS)
                .leftJoin(PRODUCTS).on(BOMS.FINISHED_PRODUCT_ID.eq(PRODUCTS.ID))
                .leftJoin(BOM_STATUSES).on(BOMS.BOM_STATUS_ID.eq(BOM_STATUSES.ID))
                .where(BOMS.ID.eq(id))
                .fetchOne();

        if (record == null) {
            return Optional.empty();
        }

        BomsRecord bomsRecord = record.into(BOMS);
        ProductsRecord productsRecord = record.into(PRODUCTS);
        BomStatusesRecord statusRecord = record.into(BOM_STATUSES);

        List<BomItem> items = fetchItemsForBom(id);

        return Optional.ofNullable(mapper.toDomain(bomsRecord, items, productsRecord, statusRecord));
    }

    @Override
    public Optional<Bom> findActiveByFinishedProductId(UUID finishedProductId) {
        Optional<UUID> activeStatusId = findStatusIdByName("ACTIVE");
        if (activeStatusId.isEmpty()) {
            return Optional.empty();
        }

        Record record = dslCtx.select(BOMS.fields())
                .select(PRODUCTS.fields())
                .select(BOM_STATUSES.fields())
                .from(BOMS)
                .leftJoin(PRODUCTS).on(BOMS.FINISHED_PRODUCT_ID.eq(PRODUCTS.ID))
                .leftJoin(BOM_STATUSES).on(BOMS.BOM_STATUS_ID.eq(BOM_STATUSES.ID))
                .where(BOMS.FINISHED_PRODUCT_ID.eq(finishedProductId))
                .and(BOMS.BOM_STATUS_ID.eq(activeStatusId.get()))
                .fetchOne();

        if (record == null) {
            return Optional.empty();
        }

        BomsRecord bomsRecord = record.into(BOMS);
        ProductsRecord productsRecord = record.into(PRODUCTS);
        BomStatusesRecord statusRecord = record.into(BOM_STATUSES);

        List<BomItem> items = fetchItemsForBom(bomsRecord.getId());

        return Optional.ofNullable(mapper.toDomain(bomsRecord, items, productsRecord, statusRecord));
    }

    @Override
    public Bom save(Bom bom) {
        BomsRecord record = mapper.toRecord(bom);
        dslCtx.insertInto(BOMS)
                .set(record)
                .onConflict(BOMS.ID)
                .doUpdate()
                .set(record)
                .execute();

        if (bom.getItems() != null && !bom.getItems().isEmpty()) {
            for (BomItem item : bom.getItems()) {
                saveItem(item);
            }
        }

        return findById(bom.getId()).orElse(bom);
    }

    @Override
    public PaginationResult<Bom> findAll(int page, int size, UUID finishedProductId, UUID bomStatusId) {
        Condition condition = DSL.trueCondition();
        if (finishedProductId != null) {
            condition = condition.and(BOMS.FINISHED_PRODUCT_ID.eq(finishedProductId));
        }
        if (bomStatusId != null) {
            condition = condition.and(BOMS.BOM_STATUS_ID.eq(bomStatusId));
        }

        int offset = page * size;
        List<Record> records = dslCtx.select(BOMS.fields())
                .select(PRODUCTS.fields())
                .select(BOM_STATUSES.fields())
                .from(BOMS)
                .leftJoin(PRODUCTS).on(BOMS.FINISHED_PRODUCT_ID.eq(PRODUCTS.ID))
                .leftJoin(BOM_STATUSES).on(BOMS.BOM_STATUS_ID.eq(BOM_STATUSES.ID))
                .where(condition)
                .orderBy(BOMS.CREATED_AT.desc())
                .limit(size)
                .offset(offset)
                .fetch();

        List<Bom> items = records.stream().map(r -> {
            BomsRecord bomsRecord = r.into(BOMS);
            ProductsRecord productsRecord = r.into(PRODUCTS);
            BomStatusesRecord statusRecord = r.into(BOM_STATUSES);
            List<BomItem> bomItems = fetchItemsForBom(bomsRecord.getId());
            return mapper.toDomain(bomsRecord, bomItems, productsRecord, statusRecord);
        }).toList();

        long total = dslCtx.fetchCount(BOMS, condition);
        return PaginationResult.<Bom>builder().total(total).items(items).build();
    }

    @Override
    public boolean existsByFinishedProductIdAndVersion(UUID finishedProductId, Integer version) {
        return dslCtx.fetchExists(
                dslCtx.selectFrom(BOMS)
                        .where(BOMS.FINISHED_PRODUCT_ID.eq(finishedProductId))
                        .and(BOMS.VERSION.eq(version)));
    }

    @Override
    public Optional<UUID> findStatusIdByName(String name) {
        return dslCtx.select(BOM_STATUSES.ID)
                .from(BOM_STATUSES)
                .where(BOM_STATUSES.NAME.eq(name))
                .fetchOptional(BOM_STATUSES.ID);
    }

    @Override
    public void deactivateActiveBomsForProduct(UUID finishedProductId, UUID activeStatusId, UUID inactiveStatusId) {
        dslCtx.update(BOMS)
                .set(BOMS.BOM_STATUS_ID, inactiveStatusId)
                .where(BOMS.FINISHED_PRODUCT_ID.eq(finishedProductId))
                .and(BOMS.BOM_STATUS_ID.eq(activeStatusId))
                .execute();
    }

    @Override
    public int countItemsByBomId(UUID bomId) {
        return dslCtx.fetchCount(
                dslCtx.selectFrom(BOM_ITEMS)
                        .where(BOM_ITEMS.BOM_ID.eq(bomId)));
    }

    @Override
    public int findMaxVersionByFinishedProductId(UUID finishedProductId) {
        Integer maxVersion = dslCtx.select(org.jooq.impl.DSL.max(BOMS.VERSION))
                .from(BOMS)
                .where(BOMS.FINISHED_PRODUCT_ID.eq(finishedProductId))
                .fetchOneInto(Integer.class);
        return maxVersion != null ? maxVersion : 0;
    }

    @Override
    public BomItem saveItem(BomItem item) {
        BomItemsRecord record = mapper.toRecord(item);
        dslCtx.insertInto(BOM_ITEMS)
                .set(record)
                .onConflict(BOM_ITEMS.ID)
                .doUpdate()
                .set(record)
                .execute();
        return item;
    }

    @Override
    public Optional<BomItem> findItemById(UUID itemId) {
        Record record = dslCtx.select(BOM_ITEMS.fields())
                .select(PRODUCTS.fields())
                .select(UNITS_OF_MEASURE.fields())
                .from(BOM_ITEMS)
                .leftJoin(PRODUCTS).on(BOM_ITEMS.MATERIAL_PRODUCT_ID.eq(PRODUCTS.ID))
                .leftJoin(UNITS_OF_MEASURE).on(BOM_ITEMS.UNIT_ID.eq(UNITS_OF_MEASURE.ID))
                .where(BOM_ITEMS.ID.eq(itemId))
                .fetchOne();

        if (record == null) {
            return Optional.empty();
        }

        BomItemsRecord itemRec = record.into(BOM_ITEMS);
        ProductsRecord matRec = record.into(PRODUCTS);
        UnitsOfMeasureRecord unitRec = record.into(UNITS_OF_MEASURE);
        return Optional.ofNullable(mapper.toDomain(itemRec, matRec, unitRec));
    }

    @Override
    public void deleteItemById(UUID itemId) {
        dslCtx.deleteFrom(BOM_ITEMS).where(BOM_ITEMS.ID.eq(itemId)).execute();
    }
}
