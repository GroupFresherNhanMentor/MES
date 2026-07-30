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
import fpt.qn.mes.workorder.application.exception.InvalidInputException;
import fpt.qn.mes.workorder.application.exception.InvalidWorkOrderStateException;
import fpt.qn.mes.workorder.application.exception.WorkOrderCodeExistsException;
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

    @Override
    @Transactional
    public WorkOrderDto updateWorkOrder(UUID id, UpdateWorkOrderRequest req) {
        WorkOrder workOrder = repository.findById(id)
                .orElseThrow(() -> new WorkOrderNotFoundException("Work Order not found with ID: " + id));

        String currentStatusName = repository.findStatusNameById(workOrder.getWorkOrderStatusId()).orElse("");
        if (!"DRAFT".equals(currentStatusName) && !"PLANNED".equals(currentStatusName)) {
            throw new InvalidWorkOrderStateException(
                    "Work Order cannot be modified in its current state. Only DRAFT and PLANNED Work Orders can be updated.");
        }

        if (req != null) {
            if (req.getPlannedQuantity() != null && req.getPlannedQuantity().compareTo(BigDecimal.ZERO) <= 0) {
                throw new InvalidInputException("Planned quantity must be greater than 0");
            }

            Instant effectiveStart = req.getPlannedStartDate() != null ? req.getPlannedStartDate() : workOrder.getPlannedStartDate();
            Instant effectiveEnd = req.getPlannedEndDate() != null ? req.getPlannedEndDate() : workOrder.getPlannedEndDate();
            if (effectiveStart != null && effectiveEnd != null && !effectiveStart.isBefore(effectiveEnd)) {
                throw new InvalidInputException("Planned start date must be before planned end date");
            }

            if (req.getCode() != null && repository.existsByCodeAndIdNot(req.getCode(), id)) {
                throw new WorkOrderCodeExistsException(
                        "Work Order code '" + req.getCode() + "' already exists");
            }
        }

        UUID targetStatusId = (req != null && req.getWorkOrderStatusId() != null) ? req.getWorkOrderStatusId() : workOrder.getWorkOrderStatusId();
        if (req != null && req.getWorkOrderStatusId() != null && !req.getWorkOrderStatusId().equals(workOrder.getWorkOrderStatusId())) {
            String targetStatusName = repository.findStatusNameById(req.getWorkOrderStatusId()).orElse("");
            if (!"DRAFT".equals(targetStatusName) && !"PLANNED".equals(targetStatusName)) {
                throw new InvalidWorkOrderStateException(
                        "PUT endpoint only supports DRAFT ⇄ PLANNED transitions. Use dedicated action endpoints for other state changes.");
            }
        }

        boolean quantityChanged = req != null && req.getPlannedQuantity() != null
                && req.getPlannedQuantity().compareTo(workOrder.getPlannedQuantity()) != 0;

        WorkOrder updatedWorkOrder = WorkOrder.builder()
                .id(workOrder.getId())
                .code(req != null && req.getCode() != null ? req.getCode() : workOrder.getCode())
                .finishedProductId(workOrder.getFinishedProductId())
                .bomId(workOrder.getBomId())
                .plannedQuantity(req != null && req.getPlannedQuantity() != null ? req.getPlannedQuantity() : workOrder.getPlannedQuantity())
                .plannedStartDate(req != null && req.getPlannedStartDate() != null ? req.getPlannedStartDate() : workOrder.getPlannedStartDate())
                .plannedEndDate(req != null && req.getPlannedEndDate() != null ? req.getPlannedEndDate() : workOrder.getPlannedEndDate())
                .priorityId(req != null && req.getPriorityId() != null ? req.getPriorityId() : workOrder.getPriorityId())
                .workOrderStatusId(targetStatusId)
                .createdBy(workOrder.getCreatedBy())
                .createdAt(workOrder.getCreatedAt())
                .build();

        WorkOrder saved = repository.update(updatedWorkOrder);

        if (quantityChanged) {
            var existingMaterials = repository.findMaterialsByWorkOrderId(id);
            var bomOptional = bomRepository.findActiveByFinishedProductId(saved.getFinishedProductId());
            if (bomOptional.isPresent()) {
                var bom = bomOptional.get();
                if (bom.getItems() != null && !bom.getItems().isEmpty()) {
                    for (var mat : existingMaterials) {
                        for (var item : bom.getItems()) {
                            if (item.getMaterialProductId().equals(mat.getMaterialProductId())) {
                                BigDecimal scrap = item.getScrapRate() != null ? item.getScrapRate() : BigDecimal.ZERO;
                                BigDecimal multiplier = BigDecimal.ONE.add(scrap);
                                BigDecimal reqQty = saved.getPlannedQuantity().multiply(item.getQuantityPerUnit()).multiply(multiplier);

                                WorkOrderMaterial updatedMat = WorkOrderMaterial.builder()
                                        .id(mat.getId())
                                        .workOrderId(mat.getWorkOrderId())
                                        .materialProductId(mat.getMaterialProductId())
                                        .requiredQuantity(reqQty)
                                        .reservedQuantity(mat.getReservedQuantity())
                                        .consumedQuantity(mat.getConsumedQuantity())
                                        .build();
                                repository.updateMaterial(updatedMat);
                                break;
                            }
                        }
                    }
                }
            }
        }

        return getWorkOrderById(saved.getId());
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
