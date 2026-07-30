package fpt.qn.mes.workorder.application.port.in;

import java.util.UUID;

import fpt.qn.mes.common.dto.response.PageResponse;
import fpt.qn.mes.workorder.application.dto.request.*;
import fpt.qn.mes.workorder.application.dto.response.*;
import fpt.qn.mes.workorder.application.dto.request.ReserveWorkOrderMaterialsRequest;
import fpt.qn.mes.workorder.application.dto.response.ReserveWorkOrderMaterialsResponse;

public interface WorkOrderUseCase {
    PageResponse<WorkOrderDto> getWorkOrders(WorkOrderSearchRequest request);
    WorkOrderDto getWorkOrderById(UUID id);
    WorkOrderDto createWorkOrder(CreateWorkOrderRequest request, UUID currentUserId);
    WorkOrderDto updateWorkOrder(UUID id, UpdateWorkOrderRequest request);
    ReserveWorkOrderMaterialsResponse reserveMaterials(UUID workOrderId, ReserveWorkOrderMaterialsRequest request);
    void deleteWorkOrder(UUID id);

    PageResponse<WorkOrderMaterialDto> getMaterials(UUID workOrderId, int page, int size);
    WorkOrderMaterialDto addMaterial(UUID workOrderId, CreateWorkOrderMaterialRequest request);
    void deleteMaterial(UUID workOrderId, UUID materialId);

    PageResponse<WorkOrderEventDto> getEvents(UUID workOrderId, int page, int size);
    WorkOrderEventDto addEvent(UUID workOrderId, CreateWorkOrderEventRequest request);
}
