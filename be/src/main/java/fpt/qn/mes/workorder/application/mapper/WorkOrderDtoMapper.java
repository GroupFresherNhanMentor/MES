package fpt.qn.mes.workorder.application.mapper;

import org.mapstruct.Mapper;

import fpt.qn.mes.workorder.application.dto.response.WorkOrderDto;
import fpt.qn.mes.workorder.application.dto.response.WorkOrderEventDto;
import fpt.qn.mes.workorder.application.dto.response.WorkOrderMaterialDto;
import fpt.qn.mes.workorder.domain.entities.WorkOrder;
import fpt.qn.mes.workorder.domain.entities.WorkOrderEvent;
import fpt.qn.mes.workorder.domain.entities.WorkOrderMaterial;

@Mapper(componentModel = "spring")
public interface WorkOrderDtoMapper {

    WorkOrderDto toDto(WorkOrder workOrder);

    WorkOrderMaterialDto toDto(WorkOrderMaterial workOrderMaterial);

    WorkOrderEventDto toDto(WorkOrderEvent workOrderEvent);
}
