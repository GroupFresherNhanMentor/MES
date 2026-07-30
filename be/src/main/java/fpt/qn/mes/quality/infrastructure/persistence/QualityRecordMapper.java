package fpt.qn.mes.quality.infrastructure.persistence;

import java.time.ZoneOffset;

import org.springframework.stereotype.Component;

import fpt.qn.mes.jooq.tables.records.DefectTypesRecord;
import fpt.qn.mes.jooq.tables.records.LotTypesRecord;
import fpt.qn.mes.jooq.tables.records.ProductsRecord;
import fpt.qn.mes.jooq.tables.records.QcActionsRecord;
import fpt.qn.mes.jooq.tables.records.QcStatusesRecord;
import fpt.qn.mes.jooq.tables.records.QualityInspectionResultsRecord;
import fpt.qn.mes.jooq.tables.records.QualityInspectionsRecord;
import fpt.qn.mes.jooq.tables.records.StockLotsRecord;
import fpt.qn.mes.jooq.tables.records.UsersRecord;
import fpt.qn.mes.jooq.tables.records.WorkOrdersRecord;
import fpt.qn.mes.quality.domain.entities.DefectType;
import fpt.qn.mes.quality.domain.entities.QcAction;
import fpt.qn.mes.quality.domain.entities.QcStatus;
import fpt.qn.mes.quality.domain.entities.QualityInspection;
import fpt.qn.mes.quality.domain.entities.QualityInspectionResult;

@Component
public class QualityRecordMapper {

    public QualityInspection toDomain(QualityInspectionsRecord r, QcStatusesRecord statusRecord,
            WorkOrdersRecord workOrderRecord, ProductsRecord productRecord,
            StockLotsRecord stockLotsRecord, LotTypesRecord lotTypesRecord) {
        return QualityInspection.builder()
            .id(r.getId())
            .workOrder(QualityInspection.WorkOrderRef.builder()
                .id(r.getWorkOrderId())
                .code(workOrderRecord.getCode())
                .build())
            .product(QualityInspection.ProductRef.builder()
                .id(r.getProductId())
                .code(productRecord.getCode())
                .name(productRecord.getName())
                .build())
            .lot(QualityInspection.StockLotRef.builder()
                .id(r.getLotId())
                .lotNumber(stockLotsRecord.getLotNumber())
                .lotType(lotTypesRecord != null ? lotTypesRecord.getName() : null)
                .build())
            .quantity(r.getQuantity())
            .qcStatus(QcStatus.builder()
                .id(statusRecord.getId())
                .name(statusRecord.getName())
                .description(statusRecord.getDescription())
                .build())
            .createdAt(r.getCreatedAt().toInstant())
            .build();
    }

    public QualityInspectionsRecord toRecord(QualityInspection i) {
        QualityInspectionsRecord r = new QualityInspectionsRecord();
        r.setId(i.getId());
        r.setWorkOrderId(i.getWorkOrder().getId());
        r.setProductId(i.getProduct().getId());
        r.setLotId(i.getLot().getId());
        r.setQuantity(i.getQuantity());
        r.setQcStatusId(i.getQcStatus().getId());
        r.setCreatedAt(i.getCreatedAt().atOffset(ZoneOffset.UTC));
        return r;
    }

    public QualityInspectionResult toDomain(QualityInspectionResultsRecord r,
            DefectTypesRecord defectTypeRecord, QcActionsRecord actionRecord, UsersRecord userRecord) {
        return QualityInspectionResult.builder()
            .id(r.getId())
            .inspectionId(r.getInspectionId())
            .isPass(r.getIsPass())
            .quantity(r.getQuantity())
            .defectType(defectTypeRecord != null
                ? DefectType.builder().id(defectTypeRecord.getId()).name(defectTypeRecord.getName()).build() : null)
            .reason(r.getReason())
            .action(actionRecord != null
                ? QcAction.builder().id(actionRecord.getId()).name(actionRecord.getName()).build() : null)
            .inspector(QualityInspectionResult.InspectorRef.builder()
                .userId(r.getInspectorId())
                .username(userRecord != null ? userRecord.getUsername() : null)
                .fullName(userRecord != null ? userRecord.getFullName() : null)
                .build())
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
        r.setDefectTypeId(i.getDefectType() != null ? i.getDefectType().getId() : null);
        r.setReason(i.getReason());
        r.setActionId(i.getAction() != null ? i.getAction().getId() : null);
        r.setInspectorId(i.getInspector() != null ? i.getInspector().getUserId() : null);
        r.setInspectedAt(i.getInspectedAt().atOffset(ZoneOffset.UTC));
        r.setNote(i.getNote());
        return r;
    }
}
