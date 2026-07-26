package fpt.qn.mes.workorder.application.mapper;

import org.springframework.stereotype.Component;

import fpt.qn.mes.workorder.application.dto.WorkOrderDto;
import fpt.qn.mes.workorder.application.dto.WorkOrderEventDto;
import fpt.qn.mes.workorder.application.dto.WorkOrderMaterialDto;
import fpt.qn.mes.workorder.domain.entities.WorkOrder;
import fpt.qn.mes.workorder.domain.entities.WorkOrderEvent;
import fpt.qn.mes.workorder.domain.entities.WorkOrderMaterial;

@Component
public class WorkOrderDtoMapper {

    public WorkOrderDto toDto(WorkOrder w) {
        return null;
    }

    public WorkOrderMaterialDto toDto(WorkOrderMaterial m) {
        return null;
    }

    public WorkOrderEventDto toDto(WorkOrderEvent e) {
        return null;
    }
}
