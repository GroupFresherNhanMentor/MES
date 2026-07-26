package fpt.qn.mes.workorder.application.service;

import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import fpt.qn.mes.common.dto.PageResponse;
import fpt.qn.mes.workorder.application.dto.CreateWorkOrderEventRequest;
import fpt.qn.mes.workorder.application.dto.CreateWorkOrderMaterialRequest;
import fpt.qn.mes.workorder.application.dto.CreateWorkOrderRequest;
import fpt.qn.mes.workorder.application.dto.UpdateWorkOrderRequest;
import fpt.qn.mes.workorder.application.dto.WorkOrderDto;
import fpt.qn.mes.workorder.application.dto.WorkOrderEventDto;
import fpt.qn.mes.workorder.application.dto.WorkOrderMaterialDto;
import fpt.qn.mes.workorder.application.mapper.WorkOrderDtoMapper;
import fpt.qn.mes.workorder.application.port.in.WorkOrderUseCase;
import fpt.qn.mes.workorder.domain.repository.WorkOrderRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class WorkOrderService implements WorkOrderUseCase {

    WorkOrderRepository repository;
    WorkOrderDtoMapper mapper;

    @Override @Transactional(readOnly = true)
    public PageResponse<WorkOrderDto> getWorkOrders(int page, int size) {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override @Transactional(readOnly = true)
    public WorkOrderDto getWorkOrderById(UUID id) {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override @Transactional
    public WorkOrderDto createWorkOrder(CreateWorkOrderRequest req, UUID currentUserId) {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override @Transactional
    public WorkOrderDto updateWorkOrder(UUID id, UpdateWorkOrderRequest req) {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override @Transactional
    public void deleteWorkOrder(UUID id) {}

    @Override @Transactional(readOnly = true)
    public PageResponse<WorkOrderMaterialDto> getMaterials(UUID workOrderId, int page, int size) {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override @Transactional
    public WorkOrderMaterialDto addMaterial(UUID workOrderId, CreateWorkOrderMaterialRequest req) {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override @Transactional
    public void deleteMaterial(UUID workOrderId, UUID materialId) {}

    @Override @Transactional(readOnly = true)
    public PageResponse<WorkOrderEventDto> getEvents(UUID workOrderId, int page, int size) {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override @Transactional
    public WorkOrderEventDto addEvent(UUID workOrderId, CreateWorkOrderEventRequest req) {
        throw new UnsupportedOperationException("Not implemented");
    }
}
