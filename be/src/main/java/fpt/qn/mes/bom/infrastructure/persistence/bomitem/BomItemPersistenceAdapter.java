package fpt.qn.mes.bom.infrastructure.persistence.bomitem;

import static fpt.qn.mes.jooq.Tables.BOM_ITEMS;
import static fpt.qn.mes.jooq.Tables.PRODUCTS;
import static fpt.qn.mes.jooq.Tables.UNITS_OF_MEASURE;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.jooq.DSLContext;
import org.springframework.stereotype.Repository;

import fpt.qn.mes.bom.domain.entities.BomItem;
import fpt.qn.mes.bom.domain.repository.BomItemRepository;
import fpt.qn.mes.common.repository.BaseRepository;
import fpt.qn.mes.jooq.tables.records.BomItemsRecord;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;

@Repository
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class BomItemPersistenceAdapter extends BaseRepository<BomItemsRecord> implements BomItemRepository {

    BomItemRecordMapper mapper;

    public BomItemPersistenceAdapter(DSLContext ctx, BomItemRecordMapper mapper) {
        super(ctx, BOM_ITEMS);
        this.mapper = mapper;
    }

    public Optional<BomItem> findById(UUID id) {
        return ctx.select()
                .from(BOM_ITEMS)
                .leftJoin(PRODUCTS).on(BOM_ITEMS.MATERIAL_PRODUCT_ID.eq(PRODUCTS.ID))
                .leftJoin(UNITS_OF_MEASURE).on(PRODUCTS.UNIT_ID.eq(UNITS_OF_MEASURE.ID))
                .where(BOM_ITEMS.ID.eq(id))
                .fetchOptional(r -> mapper.toDomain(r.into(BOM_ITEMS), r.into(PRODUCTS), r.into(UNITS_OF_MEASURE)));
    }

    public List<BomItem> findByBomId(UUID bomId) {
        return ctx.select()
                .from(BOM_ITEMS)
                .leftJoin(PRODUCTS).on(BOM_ITEMS.MATERIAL_PRODUCT_ID.eq(PRODUCTS.ID))
                .leftJoin(UNITS_OF_MEASURE).on(PRODUCTS.UNIT_ID.eq(UNITS_OF_MEASURE.ID))
                .where(BOM_ITEMS.BOM_ID.eq(bomId))
                .fetch(r -> mapper.toDomain(r.into(BOM_ITEMS), r.into(PRODUCTS), r.into(UNITS_OF_MEASURE)));
    }

    @Override
    public BomItem save(BomItem item) {
        BomItemsRecord r = mapper.toRecord(item);
        ctx.insertInto(BOM_ITEMS).set(r).onConflict(BOM_ITEMS.ID).doUpdate().set(r).execute();
        return item;
    }

    @Override
    public void deleteByBomId(UUID bomId) {
        ctx.deleteFrom(BOM_ITEMS).where(BOM_ITEMS.BOM_ID.eq(bomId)).execute();
    }
}
