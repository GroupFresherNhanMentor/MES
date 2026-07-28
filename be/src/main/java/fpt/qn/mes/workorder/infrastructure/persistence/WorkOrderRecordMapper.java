package fpt.qn.mes.workorder.infrastructure.persistence;

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
        return null;
    }

    public WorkOrderMaterial toDomain(WorkOrderMaterialsRecord r) {
        return null;
    }

    public WorkOrderMaterialsRecord toRecord(WorkOrderMaterial m) {
        return null;
    }

    public WorkOrderEvent toDomain(WorkOrderEventsRecord r) {
        return null;
    }

    public WorkOrderEventsRecord toRecord(WorkOrderEvent e) {
        return null;
    }
}
