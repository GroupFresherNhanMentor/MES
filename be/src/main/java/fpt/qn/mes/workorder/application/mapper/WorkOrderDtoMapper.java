package fpt.qn.mes.workorder.application.mapper;

import org.mapstruct.Mapper;

import fpt.qn.mes.workorder.application.dto.response.WorkOrderResponse;
import fpt.qn.mes.workorder.application.dto.response.WorkOrderEventResponse;
import fpt.qn.mes.workorder.application.dto.response.WorkOrderMaterialResponse;
import fpt.qn.mes.workorder.domain.entities.WorkOrder;
import fpt.qn.mes.workorder.domain.entities.WorkOrderEvent;
import fpt.qn.mes.workorder.domain.entities.WorkOrderMaterial;

@Mapper(componentModel = "spring")
public interface WorkOrderDtoMapper {

    WorkOrderResponse toDto(WorkOrder workOrder);

    WorkOrderMaterialResponse toDto(WorkOrderMaterial workOrderMaterial);

    WorkOrderEventResponse toDto(WorkOrderEvent workOrderEvent);
}
