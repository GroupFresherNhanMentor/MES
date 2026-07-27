package fpt.qn.mes.quality.infrastructure.persistence;

import static fpt.qn.mes.jooq.Tables.QUALITY_INSPECTIONS;

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
        super(ctx, QUALITY_INSPECTIONS); this.mapper = mapper; this.dslCtx = ctx;
    }

    @Override public Optional<QualityInspection> findById(UUID id) { throw new UnsupportedOperationException("Not implemented"); }
    @Override public QualityInspection save(QualityInspection i) { throw new UnsupportedOperationException("Not implemented"); }
    @Override public void deleteById(UUID id) {}
    @Override public PaginationResult<QualityInspection> findAll(int page, int size) { throw new UnsupportedOperationException("Not implemented"); }
    @Override public QualityInspectionResult saveResult(QualityInspectionResult result) { throw new UnsupportedOperationException("Not implemented"); }
    @Override public PaginationResult<QualityInspectionResult> findResultsByInspectionId(UUID inspectionId, int page, int size) { throw new UnsupportedOperationException("Not implemented"); }
}
