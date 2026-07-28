package fpt.qn.mes.quality.infrastructure.persistence;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import fpt.qn.mes.jooq.tables.records.QualityInspectionResultsRecord;
import fpt.qn.mes.jooq.tables.records.QualityInspectionsRecord;
import fpt.qn.mes.quality.domain.entities.QualityInspection;
import fpt.qn.mes.quality.domain.entities.QualityInspectionResult;

@Component
public class QualityRecordMapper {

    public QualityInspection toDomain(QualityInspectionsRecord r) {
        return QualityInspection.builder()
            .id(r.getId())
            .workOrderId(r.getWorkOrderId())
            .productId(r.getProductId())
            .lotId(r.getLotId())
            .quantity(r.getQuantity())
            .qcStatusId(r.getQcStatusId())
            .createdAt(r.getCreatedAt().toInstant())
            .build();
    }

    public QualityInspection toDomain(QualityInspectionsRecord r, List<QualityInspectionResultsRecord> resultRecords) {
        QualityInspection inspection = toDomain(r);
        if (resultRecords != null) {
            List<QualityInspectionResult> results = resultRecords.stream()
                .map(rr -> toDomain(rr))
                .collect(Collectors.toList());
            // Use builder to set results — since entity is immutable via @Getter
            return QualityInspection.builder()
                .id(inspection.getId())
                .workOrderId(inspection.getWorkOrderId())
                .productId(inspection.getProductId())
                .lotId(inspection.getLotId())
                .quantity(inspection.getQuantity())
                .qcStatusId(inspection.getQcStatusId())
                .createdAt(inspection.getCreatedAt())
                .results(results)
                .build();
        }
        return inspection;
    }

    public QualityInspectionsRecord toRecord(QualityInspection i) {
        QualityInspectionsRecord r = new QualityInspectionsRecord();
        r.setId(i.getId());
        r.setWorkOrderId(i.getWorkOrderId());
        r.setProductId(i.getProductId());
        r.setLotId(i.getLotId());
        r.setQuantity(i.getQuantity());
        r.setQcStatusId(i.getQcStatusId());
        r.setCreatedAt(i.getCreatedAt().atOffset(java.time.ZoneOffset.UTC));
        return r;
    }

    public QualityInspectionResult toDomain(QualityInspectionResultsRecord r) {
        return QualityInspectionResult.builder()
            .id(r.getId())
            .inspectionId(r.getInspectionId())
            .isPass(r.getIsPass())
            .quantity(r.getQuantity())
            .defectTypeId(r.getDefectTypeId())
            .reason(r.getReason())
            .actionId(r.getActionId())
            .inspectorId(r.getInspectorId())
            .inspectedAt(r.getInspectedAt().toInstant())
            .note(r.getNote())
            .build();
    }

    public QualityInspectionResultsRecord toRecord(QualityInspectionResult i) {
        QualityInspectionResultsRecord r = new QualityInspectionResultsRecord();
        r.setId(i.getId());
        r.setInspectionId(i.getInspectionId());
        r.setIsPass(i.getIsPass());
        r.setQuantity(i.getQuantity());
        r.setDefectTypeId(i.getDefectTypeId());
        r.setReason(i.getReason());
        r.setActionId(i.getActionId());
        r.setInspectorId(i.getInspectorId());
        r.setInspectedAt(i.getInspectedAt().atOffset(java.time.ZoneOffset.UTC));
        r.setNote(i.getNote());
        return r;
    }
}
