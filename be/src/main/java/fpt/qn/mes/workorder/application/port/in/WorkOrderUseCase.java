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
import fpt.qn.mes.workorder.application.dto.response.WorkOrderResponse;
import fpt.qn.mes.workorder.application.dto.response.WorkOrderEventResponse;
import fpt.qn.mes.workorder.application.dto.response.WorkOrderMaterialResponse;

public interface WorkOrderUseCase {
    PageResponse<WorkOrderResponse> getWorkOrders(WorkOrderSearchRequest request);
    WorkOrderResponse getWorkOrderById(UUID id);
    WorkOrderResponse createWorkOrder(CreateWorkOrderRequest request, UUID currentUserId);
    WorkOrderResponse updateWorkOrder(UUID id, UpdateWorkOrderRequest request);
    ReserveWorkOrderMaterialsResponse reserveMaterials(UUID workOrderId, ReserveWorkOrderMaterialsRequest request);
    WorkOrderResponse releaseMaterials(UUID workOrderId);
    WorkOrderResponse cancelWorkOrder(UUID workOrderId);
    void deleteWorkOrder(UUID id);

    PageResponse<WorkOrderMaterialResponse> getMaterials(UUID workOrderId, int page, int size);
    WorkOrderMaterialResponse addMaterial(UUID workOrderId, CreateWorkOrderMaterialRequest request);
    void deleteMaterial(UUID workOrderId, UUID materialId);

    PageResponse<WorkOrderEventResponse> getEvents(UUID workOrderId, int page, int size);
    WorkOrderEventResponse addEvent(UUID workOrderId, CreateWorkOrderEventRequest request);
}
