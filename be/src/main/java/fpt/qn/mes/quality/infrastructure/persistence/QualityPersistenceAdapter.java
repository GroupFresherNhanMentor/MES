package fpt.qn.mes.quality.infrastructure.persistence;

import static fpt.qn.mes.jooq.Tables.QUALITY_INSPECTIONS;
import static fpt.qn.mes.jooq.Tables.QUALITY_INSPECTION_RESULTS;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import org.jooq.DSLContext;
import org.springframework.stereotype.Repository;

import fpt.qn.mes.common.dto.response.PaginationResult;
import fpt.qn.mes.common.repository.BaseRepository;
import fpt.qn.mes.jooq.tables.records.QualityInspectionsRecord;
import fpt.qn.mes.quality.domain.entities.QualityInspection;
import fpt.qn.mes.quality.domain.entities.QualityInspectionResult;
import fpt.qn.mes.quality.domain.repository.QualityInspectionRepository;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;

@Repository
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class QualityPersistenceAdapter extends BaseRepository<QualityInspectionsRecord> implements QualityInspectionRepository {

    QualityRecordMapper mapper;
    DSLContext dslCtx;

    public QualityPersistenceAdapter(DSLContext ctx, QualityRecordMapper mapper) {
        super(ctx, QUALITY_INSPECTIONS);
        this.mapper = mapper;
        this.dslCtx = ctx;
    }

    @Override
    public Optional<QualityInspection> findById(UUID id) {
        return dslCtx.selectFrom(QUALITY_INSPECTIONS)
            .where(QUALITY_INSPECTIONS.ID.eq(id))
            .fetchOptional(r -> mapper.toDomain(r));
    }

    @Override
    public QualityInspection save(QualityInspection inspection) {
        QualityInspectionsRecord record = mapper.toRecord(inspection);
        dslCtx.insertInto(QUALITY_INSPECTIONS)
            .set(record)
            .onConflict(QUALITY_INSPECTIONS.ID)
            .doUpdate()
            .set(record)
            .execute();
        return inspection;
    }

    @Override
    public void deleteById(UUID id) {
        dslCtx.deleteFrom(QUALITY_INSPECTIONS)
            .where(QUALITY_INSPECTIONS.ID.eq(id))
            .execute();
    }

    @Override
    public PaginationResult<QualityInspection> findAll(int page, int size) {
        int offset = page * size;
        var items = dslCtx.selectFrom(QUALITY_INSPECTIONS)
            .limit(size)
            .offset(offset)
            .fetch(r -> mapper.toDomain(r));
        int total = dslCtx.fetchCount(QUALITY_INSPECTIONS);
        return new PaginationResult<>(total, items);
    }

    @Override
    public QualityInspectionResult saveResult(QualityInspectionResult result) {
        var record = mapper.toRecord(result);
        dslCtx.insertInto(QUALITY_INSPECTION_RESULTS)
            .set(record)
            .execute();
        return result;
    }

    @Override
    public PaginationResult<QualityInspectionResult> findResultsByInspectionId(UUID inspectionId, int page, int size) {
        int offset = page * size;
        var items = dslCtx.selectFrom(QUALITY_INSPECTION_RESULTS)
            .where(QUALITY_INSPECTION_RESULTS.INSPECTION_ID.eq(inspectionId))
            .limit(size)
            .offset(offset)
            .fetch(r -> mapper.toDomain(r));
        int total = dslCtx.fetchCount(
            QUALITY_INSPECTION_RESULTS,
            QUALITY_INSPECTION_RESULTS.INSPECTION_ID.eq(inspectionId)
        );
        return new PaginationResult<>(total, items);
    }

    @Override
    public BigDecimal sumResultQuantities(UUID inspectionId) {
        BigDecimal sum = dslCtx.select(
                QUALITY_INSPECTION_RESULTS.QUANTITY.sum()
            )
            .from(QUALITY_INSPECTION_RESULTS)
            .where(QUALITY_INSPECTION_RESULTS.INSPECTION_ID.eq(inspectionId))
            .fetchOneInto(BigDecimal.class);
        return sum != null ? sum : BigDecimal.ZERO;
    }

    @Override
    public void updateStatus(UUID inspectionId, UUID newStatusId) {
        dslCtx.update(QUALITY_INSPECTIONS)
            .set(QUALITY_INSPECTIONS.QC_STATUS_ID, newStatusId)
            .where(QUALITY_INSPECTIONS.ID.eq(inspectionId))
            .execute();
    }

    @Override
    public int countResults(UUID inspectionId) {
        return dslCtx.fetchCount(
            QUALITY_INSPECTION_RESULTS,
            QUALITY_INSPECTION_RESULTS.INSPECTION_ID.eq(inspectionId)
        );
    }
}
