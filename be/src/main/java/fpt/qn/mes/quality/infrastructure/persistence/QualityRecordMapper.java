package fpt.qn.mes.quality.infrastructure.persistence;

import org.springframework.stereotype.Component;

import fpt.qn.mes.jooq.tables.records.QualityInspectionResultsRecord;
import fpt.qn.mes.jooq.tables.records.QualityInspectionsRecord;
import fpt.qn.mes.quality.domain.entities.QualityInspection;
import fpt.qn.mes.quality.domain.entities.QualityInspectionResult;

@Component
public class QualityRecordMapper {

    public QualityInspection toDomain(QualityInspectionsRecord r) {
        return null;
    }

    public QualityInspectionsRecord toRecord(QualityInspection i) {
        return null;
    }

    public QualityInspectionResult toDomain(QualityInspectionResultsRecord r) {
        return null;
    }

    public QualityInspectionResultsRecord toRecord(QualityInspectionResult i) {
        return null;
    }
}
