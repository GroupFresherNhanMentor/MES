package fpt.qn.mes.workorder.application.port.in;

import java.util.UUID;

import fpt.qn.mes.common.dto.response.PageResponse;
import fpt.qn.mes.workorder.application.dto.request.*;
import fpt.qn.mes.workorder.application.dto.response.*;

public interface WorkOrderUseCase {
    PageResponse<WorkOrderResponse> getWorkOrders(WorkOrderSearchRequest request);
    WorkOrderResponse getWorkOrderById(UUID id);
    WorkOrderResponse createWorkOrder(CreateWorkOrderRequest request, UUID currentUserId);
    WorkOrderResponse updateWorkOrder(UUID id, UpdateWorkOrderRequest request);
    void deleteWorkOrder(UUID id);

    PageResponse<WorkOrderMaterialResponse> getMaterials(UUID workOrderId, int page, int size);
    WorkOrderMaterialResponse addMaterial(UUID workOrderId, CreateWorkOrderMaterialRequest request);
    void deleteMaterial(UUID workOrderId, UUID materialId);

    PageResponse<WorkOrderEventResponse> getEvents(UUID workOrderId, int page, int size);
    WorkOrderEventResponse addEvent(UUID workOrderId, CreateWorkOrderEventRequest request);
}
