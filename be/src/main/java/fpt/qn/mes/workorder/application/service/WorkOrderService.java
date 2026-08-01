package fpt.qn.mes.workorder.application.service;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import fpt.qn.mes.auth.application.port.out.CurrentUserPort;
import fpt.qn.mes.bom.domain.entities.Bom;
import fpt.qn.mes.bom.domain.repository.BomRepository;
import fpt.qn.mes.common.dto.response.PageResponse;
import fpt.qn.mes.inventory.domain.constants.MovementTypeConstants;
import fpt.qn.mes.inventory.domain.constants.StockStatusConstants;
import fpt.qn.mes.master.machine.application.port.in.MachineUseCase;
import fpt.qn.mes.master.warehouse.application.port.in.WarehouseUseCase;
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
import static fpt.qn.mes.workorder.application.exception.WorkOrderExceptions.*;
import fpt.qn.mes.workorder.application.mapper.WorkOrderDtoMapper;
import fpt.qn.mes.workorder.application.port.in.WorkOrderUseCase;
import fpt.qn.mes.workorder.application.port.out.AuditLogPort;
import fpt.qn.mes.workorder.application.port.out.ReservationAllocation;
import fpt.qn.mes.workorder.application.port.out.ReservationStock;
import fpt.qn.mes.workorder.application.port.out.WorkOrderReservationPort;
import fpt.qn.mes.workorder.domain.constants.WorkOrderStatusConstants;
import fpt.qn.mes.workorder.domain.entities.WorkOrder;
import fpt.qn.mes.workorder.domain.entities.WorkOrderMaterial;
import fpt.qn.mes.workorder.domain.repository.WorkOrderRepository;
import fpt.qn.mes.workorder.domain.repository.criteria.WorkOrderSearchCriteria;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class WorkOrderService implements WorkOrderUseCase {

    WorkOrderRepository repository;
    BomRepository bomRepository;
    WorkOrderDtoMapper mapper;
    WarehouseUseCase warehouseUseCase;
    MachineUseCase machineUseCase;
    WorkOrderReservationPort reservationPort;
    AuditLogPort auditLogPort;
    CurrentUserPort currentUserPort;

    @NonFinal
    @Value("${app.inventory.raw-material-warehouse-code:RAW_MATERIAL_WAREHOUSE}")
    String rawMaterialWarehouseCode;

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
        if (req == null) {
            throw new InvalidInputException("Update request body cannot be null");
        }

        WorkOrder workOrder = repository.findById(id)
                .orElseThrow(() -> new WorkOrderNotFoundException("Work Order not found with ID: " + id));

        String currentStatusName = repository.findStatusNameById(workOrder.getWorkOrderStatusId()).orElse("");
        if (!WorkOrderStatusConstants.DRAFT.equals(currentStatusName) && !WorkOrderStatusConstants.PLANNED.equals(currentStatusName)) {
            throw new InvalidWorkOrderStateException(
                    "Work Order cannot be modified in its current state. Only DRAFT and PLANNED Work Orders can be updated.");
        }

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

        UUID targetStatusId = req.getWorkOrderStatusId() != null ? req.getWorkOrderStatusId() : workOrder.getWorkOrderStatusId();
        if (req.getWorkOrderStatusId() != null && !req.getWorkOrderStatusId().equals(workOrder.getWorkOrderStatusId())) {
            String targetStatusName = repository.findStatusNameById(req.getWorkOrderStatusId()).orElse("");
            if (!WorkOrderStatusConstants.DRAFT.equals(targetStatusName) && !WorkOrderStatusConstants.PLANNED.equals(targetStatusName)) {
                throw new InvalidWorkOrderStateException(
                        "PUT endpoint only supports DRAFT ⇄ PLANNED transitions. Use dedicated action endpoints for other state changes.");
            }
        }

        boolean quantityChanged = req.getPlannedQuantity() != null
                && req.getPlannedQuantity().compareTo(workOrder.getPlannedQuantity()) != 0;

        WorkOrder updatedWorkOrder = WorkOrder.builder()
                .id(workOrder.getId())
                .code(req.getCode() != null ? req.getCode() : workOrder.getCode())
                .finishedProductId(workOrder.getFinishedProductId())
                .bomId(workOrder.getBomId())
                .plannedQuantity(req.getPlannedQuantity() != null ? req.getPlannedQuantity() : workOrder.getPlannedQuantity())
                .plannedStartDate(req.getPlannedStartDate() != null ? req.getPlannedStartDate() : workOrder.getPlannedStartDate())
                .plannedEndDate(req.getPlannedEndDate() != null ? req.getPlannedEndDate() : workOrder.getPlannedEndDate())
                .priorityId(req.getPriorityId() != null ? req.getPriorityId() : workOrder.getPriorityId())
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

    @Override
    @Transactional(noRollbackFor = InsufficientMaterialException.class)
    public ReserveWorkOrderMaterialsResponse reserveMaterials(UUID workOrderId,
            ReserveWorkOrderMaterialsRequest request) {
        if (request == null || request.getMachineId() == null) {
            throw new InvalidWorkOrderReservationException("machineId is required");
        }

        WorkOrder workOrder = repository.findForUpdate(workOrderId)
                .orElseThrow(() -> new WorkOrderNotFoundException("Work Order not found with ID: " + workOrderId));
        String currentStatus = repository.findStatusNameById(workOrder.getWorkOrderStatusId()).orElse("");
        if (!WorkOrderStatusConstants.PLANNED.equals(currentStatus)
                && !WorkOrderStatusConstants.MATERIAL_SHORTAGE.equals(currentStatus)) {
            throw new InvalidWorkOrderReservationException(
                    "Work Order must be PLANNED or MATERIAL_SHORTAGE to reserve materials");
        }

        String warehouseCode = rawMaterialWarehouseCode != null
                ? rawMaterialWarehouseCode
                : "RAW_MATERIAL_WAREHOUSE";
        var warehouse = warehouseUseCase.getWarehouseByCode(warehouseCode);
        if (!machineUseCase.isAvailableForReservation(request.getMachineId())) {
            throw new MachineNotAvailableException(
                    "Machine must be AVAILABLE before the Work Order can be reserved");
        }

        UUID availableStatusId = requireReferenceId(reservationPort.findStockStatusId(StockStatusConstants.AVAILABLE),
                "AVAILABLE stock status is not configured");
        UUID reservedStatusId = requireReferenceId(reservationPort.findStockStatusId(StockStatusConstants.RESERVED),
                "RESERVED stock status is not configured");
        UUID movementTypeId = requireReferenceId(reservationPort.findMovementTypeId(MovementTypeConstants.RESERVE),
                "RESERVE movement type is not configured");

        List<WorkOrderMaterial> materials = repository.findMaterialsByWorkOrderId(workOrderId);
        List<UUID> productIds = materials.stream()
                .map(WorkOrderMaterial::getMaterialProductId)
                .distinct()
                .toList();
        List<ReservationStock> stock = reservationPort.findAvailableStock(
                warehouse.getId(), productIds, availableStatusId);

        Map<UUID, BigDecimal> remainingStockMap = new HashMap<>();
        for (ReservationStock item : stock) {
            remainingStockMap.put(item.balanceId(), item.quantity());
        }

        List<ReservationAllocation> allocations = new ArrayList<>();
        Map<UUID, BigDecimal> reservedByProduct = new HashMap<>();
        List<ShortageDetail> shortages = new ArrayList<>();
        for (WorkOrderMaterial material : materials) {
            BigDecimal required = material.getRequiredQuantity() == null
                    ? BigDecimal.ZERO
                    : material.getRequiredQuantity();
            BigDecimal alreadyReserved = material.getReservedQuantity() == null
                    ? BigDecimal.ZERO
                    : material.getReservedQuantity();
            BigDecimal remaining = required.subtract(alreadyReserved);
            if (remaining.compareTo(BigDecimal.ZERO) <= 0) {
                continue;
            }

            BigDecimal available = stock.stream()
                    .filter(item -> item.productId().equals(material.getMaterialProductId()))
                    .map(item -> remainingStockMap.getOrDefault(item.balanceId(), BigDecimal.ZERO))
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            if (available.compareTo(remaining) < 0) {
                shortages.add(new ShortageDetail(material.getMaterialProductId(), remaining, available,
                        remaining.subtract(available)));
                continue;
            }

            BigDecimal left = remaining;
            for (ReservationStock item : stock) {
                if (!item.productId().equals(material.getMaterialProductId())
                        || left.compareTo(BigDecimal.ZERO) <= 0) {
                    continue;
                }
                BigDecimal availInItem = remainingStockMap.getOrDefault(item.balanceId(), BigDecimal.ZERO);
                if (availInItem.compareTo(BigDecimal.ZERO) <= 0) {
                    continue;
                }
                BigDecimal amount = availInItem.min(left);
                allocations.add(new ReservationAllocation(item.balanceId(), item.warehouseId(), item.locationId(),
                        item.productId(), item.lotId(), amount));
                remainingStockMap.put(item.balanceId(), availInItem.subtract(amount));
                left = left.subtract(amount);
            }
            reservedByProduct.merge(material.getMaterialProductId(), remaining, BigDecimal::add);
        }

        UUID actorId = currentUserPort.getCurrentUserId();
        if (!shortages.isEmpty()) {
            UUID shortageStatusId = requireReferenceId(
                    repository.findStatusIdByName(WorkOrderStatusConstants.MATERIAL_SHORTAGE).orElse(null),
                    "MATERIAL_SHORTAGE status is not configured");
            updateStatus(workOrder, shortageStatusId);
            auditLogPort.recordStatusTransition(actorId, workOrderId, currentStatus,
                    WorkOrderStatusConstants.MATERIAL_SHORTAGE);
            throw new InsufficientMaterialException(
                    "Insufficient stock for one or more Work Order materials", shortages);
        }

        reservationPort.applyReservation(workOrderId, allocations, reservedByProduct, availableStatusId,
                reservedStatusId, movementTypeId, actorId);
        UUID readyStatusId = requireReferenceId(
                repository.findStatusIdByName(WorkOrderStatusConstants.READY_TO_PRODUCE).orElse(null),
                "READY_TO_PRODUCE status is not configured");
        updateStatus(workOrder, readyStatusId);
        auditLogPort.recordStatusTransition(actorId, workOrderId, currentStatus,
                WorkOrderStatusConstants.READY_TO_PRODUCE);
        return ReserveWorkOrderMaterialsResponse.builder()
                .workOrderId(workOrderId)
                .status(WorkOrderStatusConstants.READY_TO_PRODUCE)
                .build();
    }

    private void updateStatus(WorkOrder workOrder, UUID statusId) {
        repository.update(WorkOrder.builder()
                .id(workOrder.getId())
                .code(workOrder.getCode())
                .finishedProductId(workOrder.getFinishedProductId())
                .bomId(workOrder.getBomId())
                .plannedQuantity(workOrder.getPlannedQuantity())
                .plannedStartDate(workOrder.getPlannedStartDate())
                .plannedEndDate(workOrder.getPlannedEndDate())
                .priorityId(workOrder.getPriorityId())
                .workOrderStatusId(statusId)
                .createdBy(workOrder.getCreatedBy())
                .createdAt(workOrder.getCreatedAt())
                .build());
    }

    private UUID requireReferenceId(UUID id, String message) {
        if (id == null) {
            throw new InvalidWorkOrderReservationException(message);
        }
        return id;
    }

    @Override
    @Transactional
    public WorkOrderDto releaseMaterials(UUID workOrderId) {
        // Serialize state transitions for this work order before touching its reservations.
        WorkOrder workOrder = repository.findForUpdate(workOrderId)
                .orElseThrow(() -> new WorkOrderNotFoundException("Work Order not found with ID: " + workOrderId));

        String currentStatus = repository.findStatusNameById(workOrder.getWorkOrderStatusId()).orElse("");
        if (!WorkOrderStatusConstants.READY_TO_PRODUCE.equals(currentStatus)
                && !WorkOrderStatusConstants.PLANNED.equals(currentStatus)) {
            throw new InvalidWorkOrderStateException(
                    "Cannot release materials for work order in " + currentStatus + " status");
        }

        UUID availableStatusId = requireReferenceId(reservationPort.findStockStatusId(StockStatusConstants.AVAILABLE),
                "AVAILABLE stock status is not configured");
        UUID reservedStatusId = requireReferenceId(reservationPort.findStockStatusId(StockStatusConstants.RESERVED),
                "RESERVED stock status is not configured");
        UUID releaseMovementTypeId = requireReferenceId(
                reservationPort.findMovementTypeId(MovementTypeConstants.RELEASE_RESERVATION),
                "RELEASE_RESERVATION movement type is not configured");

        UUID actorId = currentUserPort.getCurrentUserId();

        // Returning false means the adapter detected an unsafe balance; throw to roll back the transaction.
        if (!reservationPort.releaseReservation(workOrderId, availableStatusId, reservedStatusId,
                releaseMovementTypeId, actorId)) {
            throw new InvalidWorkOrderStateException("Reserved material balance is inconsistent");
        }

        if (WorkOrderStatusConstants.READY_TO_PRODUCE.equals(currentStatus)) {
            // A successful material release returns a ready work order to planning.
            UUID plannedStatusId = requireReferenceId(
                    repository.findStatusIdByName(WorkOrderStatusConstants.PLANNED).orElse(null),
                    "PLANNED status is not configured");
            updateStatus(workOrder, plannedStatusId);
            auditLogPort.recordStatusTransition(actorId, workOrderId, currentStatus,
                    WorkOrderStatusConstants.PLANNED, "RELEASE_MATERIAL");
        }

        return getWorkOrderById(workOrderId);
    }

    @Override
    @Transactional
    public WorkOrderDto cancelWorkOrder(UUID workOrderId) {
        // Lock first so cancellation cannot race a release or production transition.
        WorkOrder workOrder = repository.findForUpdate(workOrderId)
                .orElseThrow(() -> new WorkOrderNotFoundException("Work Order not found with ID: " + workOrderId));

        String currentStatus = repository.findStatusNameById(workOrder.getWorkOrderStatusId()).orElse("");
        if (!WorkOrderStatusConstants.DRAFT.equals(currentStatus)
                && !WorkOrderStatusConstants.PLANNED.equals(currentStatus)
                && !WorkOrderStatusConstants.MATERIAL_SHORTAGE.equals(currentStatus)
                && !WorkOrderStatusConstants.READY_TO_PRODUCE.equals(currentStatus)) {
            throw new InvalidWorkOrderStateException(
                    "Cannot cancel work order in " + currentStatus + " status");
        }

        UUID availableStatusId = requireReferenceId(reservationPort.findStockStatusId(StockStatusConstants.AVAILABLE),
                "AVAILABLE stock status is not configured");
        UUID reservedStatusId = requireReferenceId(reservationPort.findStockStatusId(StockStatusConstants.RESERVED),
                "RESERVED stock status is not configured");
        UUID releaseMovementTypeId = requireReferenceId(
                reservationPort.findMovementTypeId(MovementTypeConstants.RELEASE_RESERVATION),
                "RELEASE_RESERVATION movement type is not configured");

        UUID actorId = currentUserPort.getCurrentUserId();

        // Cancellation must release all outstanding reservations before changing the work order state.
        if (!reservationPort.releaseReservation(workOrderId, availableStatusId, reservedStatusId,
                releaseMovementTypeId, actorId)) {
            throw new InvalidWorkOrderStateException("Reserved material balance is inconsistent");
        }

        // Change status only after reservation release succeeds in the same transaction.
        UUID cancelledStatusId = requireReferenceId(
                repository.findStatusIdByName(WorkOrderStatusConstants.CANCELLED).orElse(null),
                "CANCELLED status is not configured");
        updateStatus(workOrder, cancelledStatusId);
        auditLogPort.recordStatusTransition(actorId, workOrderId, currentStatus,
                WorkOrderStatusConstants.CANCELLED, "CANCEL_WORK_ORDER");

        return getWorkOrderById(workOrderId);
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
