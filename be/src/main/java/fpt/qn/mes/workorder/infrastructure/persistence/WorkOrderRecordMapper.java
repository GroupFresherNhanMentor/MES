package fpt.qn.mes.workorder.infrastructure.persistence;

import java.time.ZoneOffset;

import org.springframework.stereotype.Component;

import fpt.qn.mes.jooq.tables.records.WorkOrderEventsRecord;
import fpt.qn.mes.jooq.tables.records.WorkOrderMaterialsRecord;
import fpt.qn.mes.jooq.tables.records.WorkOrdersRecord;
import fpt.qn.mes.workorder.domain.entities.WorkOrder;
import fpt.qn.mes.workorder.domain.entities.WorkOrderEvent;
import fpt.qn.mes.workorder.domain.entities.WorkOrderMaterial;

@Component
public class WorkOrderRecordMapper {

    public WorkOrder toDomain(WorkOrdersRecord r) {
        if (r == null) return null;
        return WorkOrder.builder()
                .id(r.getId())
                .code(r.getCode())
                .finishedProductId(r.getFinishedProductId())
                .bomId(r.getBomId())
                .plannedQuantity(r.getPlannedQuantity())
                .plannedStartDate(r.getPlannedStartDate() != null ? r.getPlannedStartDate().toInstant() : null)
                .plannedEndDate(r.getPlannedEndDate() != null ? r.getPlannedEndDate().toInstant() : null)
                .priorityId(r.getPriorityId())
                .workOrderStatusId(r.getWorkOrderStatusId())
                .createdBy(r.getCreatedBy())
                .createdAt(r.getCreatedAt() != null ? r.getCreatedAt().toInstant() : null)
                .build();
    }

    public WorkOrdersRecord toRecord(WorkOrder w) {
        if (w == null) return null;
        WorkOrdersRecord r = new WorkOrdersRecord();
        r.setId(w.getId());
        r.setCode(w.getCode());
        r.setFinishedProductId(w.getFinishedProductId());
        r.setBomId(w.getBomId());
        r.setPlannedQuantity(w.getPlannedQuantity());
        r.setPlannedStartDate(w.getPlannedStartDate() != null ? w.getPlannedStartDate().atOffset(ZoneOffset.UTC) : null);
        r.setPlannedEndDate(w.getPlannedEndDate() != null ? w.getPlannedEndDate().atOffset(ZoneOffset.UTC) : null);
        r.setPriorityId(w.getPriorityId());
        r.setWorkOrderStatusId(w.getWorkOrderStatusId());
        r.setCreatedBy(w.getCreatedBy());
        r.setCreatedAt(w.getCreatedAt() != null ? w.getCreatedAt().atOffset(ZoneOffset.UTC) : null);
        return r;
    }

    public WorkOrderMaterial toDomain(WorkOrderMaterialsRecord r) {
        if (r == null) return null;
        return WorkOrderMaterial.builder()
                .id(r.getId())
                .workOrderId(r.getWorkOrderId())
                .materialProductId(r.getMaterialProductId())
                .requiredQuantity(r.getRequiredQuantity())
                .reservedQuantity(r.getReservedQuantity())
                .consumedQuantity(r.getConsumedQuantity())
                .build();
    }

    public WorkOrderMaterialsRecord toRecord(WorkOrderMaterial m) {
        if (m == null) return null;
        WorkOrderMaterialsRecord r = new WorkOrderMaterialsRecord();
        r.setId(m.getId());
        r.setWorkOrderId(m.getWorkOrderId());
        r.setMaterialProductId(m.getMaterialProductId());
        r.setRequiredQuantity(m.getRequiredQuantity());
        r.setReservedQuantity(m.getReservedQuantity());
        r.setConsumedQuantity(m.getConsumedQuantity());
        return r;
    }

    public WorkOrderEvent toDomain(WorkOrderEventsRecord r) {
        if (r == null) return null;
        return WorkOrderEvent.builder()
                .id(r.getId())
                .workOrderId(r.getWorkOrderId())
                .eventTypeId(r.getEventTypeId())
                .operatorId(r.getOperatorId())
                .eventTimestamp(r.getEventTimestamp() != null ? r.getEventTimestamp().toInstant() : null)
                .note(r.getNote())
                .build();
    }

    public WorkOrderEventsRecord toRecord(WorkOrderEvent e) {
        return null;
    }
}
