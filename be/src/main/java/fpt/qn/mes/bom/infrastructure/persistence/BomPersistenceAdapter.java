package fpt.qn.mes.bom.infrastructure.persistence;

import static fpt.qn.mes.jooq.Tables.BOMS;
import static fpt.qn.mes.jooq.Tables.BOM_ITEMS;
import static fpt.qn.mes.jooq.Tables.BOM_STATUSES;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.jooq.DSLContext;
import org.springframework.stereotype.Repository;

import fpt.qn.mes.bom.domain.entities.Bom;
import fpt.qn.mes.bom.domain.entities.BomItem;
import fpt.qn.mes.bom.domain.repository.BomRepository;
import fpt.qn.mes.common.dto.response.PaginationResult;
import fpt.qn.mes.common.repository.BaseRepository;
import fpt.qn.mes.jooq.tables.records.BomItemsRecord;
import fpt.qn.mes.jooq.tables.records.BomsRecord;
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

    @Override
    public Optional<Bom> findById(UUID id) {
        BomsRecord record = dslCtx.selectFrom(BOMS)
                .where(BOMS.ID.eq(id))
                .fetchOne();

        if (record == null) {
            return Optional.empty();
        }

        List<BomItem> items = dslCtx.selectFrom(BOM_ITEMS)
                .where(BOM_ITEMS.BOM_ID.eq(id))
                .fetch(mapper::toDomain);

        return Optional.ofNullable(mapper.toDomain(record, items));
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
    public PaginationResult<Bom> findAll(int page, int size) {
        int offset = page * size;
        List<Bom> items = dslCtx.selectFrom(BOMS)
                .limit(size)
                .offset(offset)
                .fetch(r -> mapper.toDomain(r));
        long total = dslCtx.fetchCount(BOMS);
        return new PaginationResult<>(total, items);
    }

    @Override
    public boolean existsByFinishedProductIdAndVersion(UUID finishedProductId, Integer version) {
        return dslCtx.fetchExists(
                dslCtx.selectFrom(BOMS)
                        .where(BOMS.FINISHED_PRODUCT_ID.eq(finishedProductId))
                        .and(BOMS.VERSION.eq(version))
        );
    }

    @Override
    public Optional<UUID> findStatusIdByName(String name) {
        return dslCtx.select(BOM_STATUSES.ID)
                .from(BOM_STATUSES)
                .where(BOM_STATUSES.NAME.eq(name))
                .fetchOptional(BOM_STATUSES.ID);
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
        return dslCtx.selectFrom(BOM_ITEMS)
                .where(BOM_ITEMS.ID.eq(itemId))
                .fetchOptional(mapper::toDomain);
    }

    @Override
    public void deleteItemById(UUID itemId) {
        dslCtx.deleteFrom(BOM_ITEMS).where(BOM_ITEMS.ID.eq(itemId)).execute();
    }
}
