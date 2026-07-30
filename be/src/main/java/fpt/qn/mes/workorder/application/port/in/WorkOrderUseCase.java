package fpt.qn.mes.workorder.application.port.in;

import java.util.UUID;

import fpt.qn.mes.common.dto.response.PageResponse;
import fpt.qn.mes.workorder.application.dto.request.CreateWorkOrderEventRequest;
import fpt.qn.mes.workorder.application.dto.request.CreateWorkOrderMaterialRequest;
import fpt.qn.mes.workorder.application.dto.request.CreateWorkOrderRequest;
import fpt.qn.mes.workorder.application.dto.request.ReserveWorkOrderMaterialsRequest;
import fpt.qn.mes.workorder.application.dto.request.UpdateWorkOrderRequest;
import fpt.qn.mes.workorder.application.dto.request.WorkOrderSearchRequest;
import fpt.qn.mes.workorder.application.dto.response.ReserveWorkOrderMaterialsResponse;
import fpt.qn.mes.workorder.application.dto.response.WorkOrderDto;
import fpt.qn.mes.workorder.application.dto.response.WorkOrderEventDto;
import fpt.qn.mes.workorder.application.dto.response.WorkOrderMaterialDto;

public interface WorkOrderUseCase {
    PageResponse<WorkOrderDto> getWorkOrders(WorkOrderSearchRequest request);
    WorkOrderDto getWorkOrderById(UUID id);
    WorkOrderDto createWorkOrder(CreateWorkOrderRequest request, UUID currentUserId);
    WorkOrderDto updateWorkOrder(UUID id, UpdateWorkOrderRequest request);
    ReserveWorkOrderMaterialsResponse reserveMaterials(UUID workOrderId, ReserveWorkOrderMaterialsRequest request);
    WorkOrderDto releaseMaterials(UUID workOrderId);
    WorkOrderDto cancelWorkOrder(UUID workOrderId);
    void deleteWorkOrder(UUID id);

    PageResponse<WorkOrderMaterialDto> getMaterials(UUID workOrderId, int page, int size);
    WorkOrderMaterialDto addMaterial(UUID workOrderId, CreateWorkOrderMaterialRequest request);
    void deleteMaterial(UUID workOrderId, UUID materialId);

    PageResponse<WorkOrderEventDto> getEvents(UUID workOrderId, int page, int size);
    WorkOrderEventDto addEvent(UUID workOrderId, CreateWorkOrderEventRequest request);
}
