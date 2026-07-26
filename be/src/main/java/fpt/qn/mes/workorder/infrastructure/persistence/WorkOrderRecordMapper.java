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
        return null;
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
