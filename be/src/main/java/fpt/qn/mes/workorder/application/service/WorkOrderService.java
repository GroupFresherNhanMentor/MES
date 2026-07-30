package fpt.qn.mes.workorder.application.service;

import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import fpt.qn.mes.common.dto.response.PageResponse;
import fpt.qn.mes.workorder.application.dto.request.CreateWorkOrderEventRequest;
import fpt.qn.mes.workorder.application.dto.request.CreateWorkOrderMaterialRequest;
import fpt.qn.mes.workorder.application.dto.request.CreateWorkOrderRequest;
import fpt.qn.mes.workorder.application.dto.request.UpdateWorkOrderRequest;
import fpt.qn.mes.workorder.application.dto.response.WorkOrderDto;
import fpt.qn.mes.workorder.application.dto.response.WorkOrderEventDto;
import fpt.qn.mes.workorder.application.dto.response.WorkOrderMaterialDto;
import fpt.qn.mes.workorder.application.mapper.WorkOrderDtoMapper;
import fpt.qn.mes.workorder.application.port.in.WorkOrderUseCase;
import fpt.qn.mes.workorder.domain.repository.WorkOrderRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.time.Instant;
import fpt.qn.mes.bom.domain.entities.Bom;
import fpt.qn.mes.bom.domain.repository.BomRepository;
import fpt.qn.mes.workorder.application.dto.request.WorkOrderSearchRequest;
import fpt.qn.mes.workorder.application.exception.BomNotActiveException;
import fpt.qn.mes.workorder.application.exception.WorkOrderNotFoundException;
import fpt.qn.mes.workorder.domain.entities.WorkOrder;
import fpt.qn.mes.workorder.domain.entities.WorkOrderMaterial;
import fpt.qn.mes.workorder.domain.repository.criteria.WorkOrderSearchCriteria;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class WorkOrderService implements WorkOrderUseCase {

    WorkOrderRepository repository;
    BomRepository bomRepository;
    WorkOrderDtoMapper mapper;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<WorkOrderDto> getWorkOrders(WorkOrderSearchRequest request) {
        WorkOrderSearchCriteria criteria = WorkOrderSearchCriteria.builder()
                .page(request != null ? request.getPage() : 0)
                .size(request != null ? request.getSize() : 20)
                .finishedProductId(request != null ? request.getFinishedProductId() : null)
                .statusId(request != null ? request.getStatusId() : null)
                .code(request != null ? request.getCode() : null)
                .build();

        var result = repository.findAll(criteria);
        var dtos = result.getItems().stream()
                .map(w -> mapper.toDto(w))
                .toList();

        return PageResponse.<WorkOrderDto>of(dtos, result.getTotal(), criteria.getPage(), criteria.getSize());
    }

    @Override
    @Transactional(readOnly = true)
    public WorkOrderDto getWorkOrderById(UUID id) {
        var workOrder = repository.findById(id)
                .orElseThrow(() -> new WorkOrderNotFoundException("Work Order not found with ID: " + id));

        var materials = repository.findMaterialsByWorkOrderId(id).stream()
                .map(m -> mapper.toDto(m))
                .toList();

        var events = repository.findEventsByWorkOrderId(id).stream()
                .map(e -> mapper.toDto(e))
                .toList();

        WorkOrderDto baseDto = mapper.toDto(workOrder);

        return WorkOrderDto.builder()
                .id(baseDto.getId())
                .code(baseDto.getCode())
                .finishedProductId(baseDto.getFinishedProductId())
                .bomId(baseDto.getBomId())
                .plannedQuantity(baseDto.getPlannedQuantity())
                .plannedStartDate(baseDto.getPlannedStartDate())
                .plannedEndDate(baseDto.getPlannedEndDate())
                .priorityId(baseDto.getPriorityId())
                .workOrderStatusId(baseDto.getWorkOrderStatusId())
                .createdBy(baseDto.getCreatedBy())
                .createdAt(baseDto.getCreatedAt())
                .materials(materials)
                .events(events)
                .build();
    }

    @Override
    @Transactional
    public WorkOrderDto createWorkOrder(CreateWorkOrderRequest req, UUID currentUserId) {
        Bom activeBom = bomRepository.findActiveByFinishedProductId(req.getFinishedProductId())
                .orElseThrow(() -> new BomNotActiveException("No active BOM found for finished product: " + req.getFinishedProductId()));

        WorkOrder workOrder = WorkOrder.builder()
                .code(req.getCode())
                .finishedProductId(req.getFinishedProductId())
                .bomId(activeBom.getId())
                .plannedQuantity(req.getPlannedQuantity())
                .plannedStartDate(req.getPlannedStartDate())
                .plannedEndDate(req.getPlannedEndDate())
                .priorityId(req.getPriorityId())
                .workOrderStatusId(req.getWorkOrderStatusId())
                .createdBy(currentUserId)
                .createdAt(Instant.now())
                .build();

        WorkOrder saved = repository.save(workOrder);

        if (activeBom.getItems() != null && !activeBom.getItems().isEmpty()) {
            for (var item : activeBom.getItems()) {
                BigDecimal scrap = item.getScrapRate() != null ? item.getScrapRate() : BigDecimal.ZERO;
                BigDecimal multiplier = BigDecimal.ONE.add(scrap);
                BigDecimal reqQty = req.getPlannedQuantity().multiply(item.getQuantityPerUnit()).multiply(multiplier);

                WorkOrderMaterial mat = WorkOrderMaterial.builder()
                        .workOrderId(saved.getId())
                        .materialProductId(item.getMaterialProductId())
                        .requiredQuantity(reqQty)
                        .reservedQuantity(BigDecimal.ZERO)
                        .consumedQuantity(BigDecimal.ZERO)
                        .build();

                repository.saveMaterial(mat);
            }
        }

        return mapper.toDto(saved);
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
