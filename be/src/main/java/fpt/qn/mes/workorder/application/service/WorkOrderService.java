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
import fpt.qn.mes.workorder.application.dto.request.CreateWorkOrderEventRequest;
import fpt.qn.mes.workorder.application.dto.request.CreateWorkOrderMaterialRequest;
import fpt.qn.mes.workorder.application.dto.request.CreateWorkOrderRequest;
import fpt.qn.mes.workorder.application.dto.request.UpdateWorkOrderRequest;
import fpt.qn.mes.workorder.application.dto.request.WorkOrderSearchRequest;
import fpt.qn.mes.workorder.application.dto.response.ReserveWorkOrderMaterialsResponse;
import fpt.qn.mes.workorder.application.dto.response.WorkOrderEventResponse;
import fpt.qn.mes.workorder.application.dto.response.WorkOrderMaterialResponse;
import fpt.qn.mes.workorder.application.dto.response.WorkOrderResponse;
import fpt.qn.mes.workorder.application.exception.ShortageDetail;
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

/**
 * Service implementation for managing Work Orders in the Manufacturing Execution System (MES).
 * Handles core business logic including creation, updates, material requirements calculation,
 * and FIFO material reservation.
 */
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class WorkOrderService implements WorkOrderUseCase {

    // Domain repositories and mappers
    WorkOrderRepository repository;
    BomRepository bomRepository;
    WorkOrderDtoMapper mapper;

    // Outbound ports and external domain use cases
    MachineUseCase machineUseCase;
    WorkOrderReservationPort reservationPort;
    AuditLogPort auditLogPort;
    CurrentUserPort currentUserPort;

    /**
     * Retrieves a paginated list of Work Orders based on search criteria.
     *
     * @param request Search request containing page, size, and optional filters
     * @return Paginated response of WorkOrderResponse DTOs
     */
    @Override
    @Transactional(readOnly = true)
    public PageResponse<WorkOrderResponse> getWorkOrders(WorkOrderSearchRequest request) {
        // Build domain search criteria from input request
        WorkOrderSearchCriteria criteria = WorkOrderSearchCriteria.builder()
                .page(request != null ? request.getPage() : 0)
                .size(request != null ? request.getSize() : 20)
                .finishedProductId(request != null ? request.getFinishedProductId() : null)
                .statusId(request != null ? request.getStatusId() : null)
                .code(request != null ? request.getCode() : null)
                .build();

        // Query database repository and map entities to DTOs
        var result = repository.findAll(criteria);
        var dtos = result.getItems().stream()
                .map(w -> mapper.toDto(w))
                .toList();

        return PageResponse.<WorkOrderResponse>of(dtos, result.getTotal(), criteria.getPage(), criteria.getSize());
    }

    /**
     * Retrieves a single Work Order by its unique ID, including associated materials and events.
     *
     * @param id Work Order UUID
     * @return Full WorkOrderResponse DTO with materials and events list
     */
    @Override
    @Transactional(readOnly = true)
    public WorkOrderResponse getWorkOrderById(UUID id) {
        // Find Work Order entity or throw exception if not found
        var workOrder = repository.findById(id)
                .orElseThrow(() -> new WorkOrderNotFoundException("Work Order not found with ID: " + id));

        // Fetch associated materials for this Work Order
        var materials = repository.findMaterialsByWorkOrderId(id).stream()
                .map(m -> mapper.toDto(m))
                .toList();

        // Fetch associated audit events for this Work Order
        var events = repository.findEventsByWorkOrderId(id).stream()
                .map(e -> mapper.toDto(e))
                .toList();

        WorkOrderResponse baseDto = mapper.toDto(workOrder);

        // Build composite response DTO
        return WorkOrderResponse.builder()
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

    /**
     * Creates a new Work Order and calculates material requirements based on the active BOM.
     *
     * @param req Request DTO containing Work Order creation details
     * @param currentUserId ID of the user creating the Work Order
     * @return Created WorkOrderResponse DTO
     */
    @Override
    @Transactional
    public WorkOrderResponse createWorkOrder(CreateWorkOrderRequest req, UUID currentUserId) {
        // Fetch active Bill of Materials (BOM) for the finished product
        Bom activeBom = bomRepository.findActiveByFinishedProductId(req.getFinishedProductId())
                .orElseThrow(() -> new BomNotActiveException("No active BOM found for finished product: " + req.getFinishedProductId()));

        // Construct Work Order domain entity
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

        // Save Work Order to database
        WorkOrder saved = repository.save(workOrder);

        // Calculate and save required materials based on BOM items and scrap rates
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

    /**
     * Updates an existing Work Order and recalculates required materials if planned quantity changes.
     *
     * @param id Work Order UUID
     * @param req Update request DTO
     * @return Updated WorkOrderResponse DTO
     */
    @Override
    @Transactional
    public WorkOrderResponse updateWorkOrder(UUID id, UpdateWorkOrderRequest req) {
        // Validate request body
        if (req == null) {
            throw new InvalidInputException("Update request body cannot be null");
        }

        // Fetch Work Order entity
        WorkOrder workOrder = repository.findById(id)
                .orElseThrow(() -> new WorkOrderNotFoundException("Work Order not found with ID: " + id));

        // Enforce state rule: Only DRAFT and PLANNED Work Orders can be updated
        String currentStatusName = repository.findStatusNameById(workOrder.getWorkOrderStatusId()).orElse("");
        if (!WorkOrderStatusConstants.DRAFT.equals(currentStatusName) && !WorkOrderStatusConstants.PLANNED.equals(currentStatusName)) {
            throw new InvalidWorkOrderStateException(
                    "Work Order cannot be modified in its current state. Only DRAFT and PLANNED Work Orders can be updated.");
        }

        // Validate planned quantity
        if (req.getPlannedQuantity() != null && req.getPlannedQuantity().compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidInputException("Planned quantity must be greater than 0");
        }

        // Validate date range logic
        Instant effectiveStart = req.getPlannedStartDate() != null ? req.getPlannedStartDate() : workOrder.getPlannedStartDate();
        Instant effectiveEnd = req.getPlannedEndDate() != null ? req.getPlannedEndDate() : workOrder.getPlannedEndDate();
        if (effectiveStart != null && effectiveEnd != null && !effectiveStart.isBefore(effectiveEnd)) {
            throw new InvalidInputException("Planned start date must be before planned end date");
        }

        // Check for duplicate Work Order code
        if (req.getCode() != null && repository.existsByCodeAndIdNot(req.getCode(), id)) {
            throw new WorkOrderCodeExistsException(
                    "Work Order code '" + req.getCode() + "' already exists");
        }

        // Enforce state transition rules via PUT endpoint (only DRAFT <-> PLANNED allowed)
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

        // Build updated entity
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

        // Recalculate material requirements if planned quantity has changed
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

    /**
     * Reserves raw materials from inventory for a Work Order using FIFO stock allocation.
     * Transitions Work Order to READY_TO_PRODUCE if stock is sufficient, or MATERIAL_SHORTAGE if insufficient.
     * Machine status is checked later during production start.
     *
     * @param workOrderId Work Order UUID
     * @return ReserveWorkOrderMaterialsResponse DTO
     */
    @Override
    @Transactional(noRollbackFor = InsufficientMaterialException.class)
    public ReserveWorkOrderMaterialsResponse reserveMaterials(UUID workOrderId) {
        // Step 1: Lock Work Order record with pessimistic lock (FOR UPDATE) and validate status (PLANNED or MATERIAL_SHORTAGE)
        WorkOrder workOrder = repository.findForUpdate(workOrderId)
                .orElseThrow(() -> new WorkOrderNotFoundException("Work Order not found with ID: " + workOrderId));
        String currentStatus = repository.findStatusNameById(workOrder.getWorkOrderStatusId()).orElse("");
        if (!WorkOrderStatusConstants.PLANNED.equals(currentStatus)
                && !WorkOrderStatusConstants.MATERIAL_SHORTAGE.equals(currentStatus)) {
            throw new InvalidWorkOrderReservationException(
                    "Work Order must be PLANNED or MATERIAL_SHORTAGE to reserve materials");
        }

        // Step 2: Resolve reference system IDs for stock status and movement types
        UUID availableStatusId = requireReferenceId(reservationPort.findStockStatusId(StockStatusConstants.AVAILABLE),
                "AVAILABLE stock status is not configured");
        UUID reservedStatusId = requireReferenceId(reservationPort.findStockStatusId(StockStatusConstants.RESERVED),
                "RESERVED stock status is not configured");
        UUID movementTypeId = requireReferenceId(reservationPort.findMovementTypeId(MovementTypeConstants.RESERVE),
                "RESERVE movement type is not configured");

        // Step 3: Query available stock across all ACTIVE warehouses in pure FIFO date order
        List<WorkOrderMaterial> materials = repository.findMaterialsByWorkOrderId(workOrderId);
        List<UUID> productIds = materials.stream()
                .map(WorkOrderMaterial::getMaterialProductId)
                .distinct()
                .toList();

        // Return list of available lots across ACTIVE warehouses ordered by stock_lots.created_at ASC
        List<ReservationStock> stock = reservationPort.findAvailableStock(productIds, availableStatusId);

        // Step 4: Initialize in-memory tracker for remaining available stock per balance ID
        Map<UUID, BigDecimal> remainingStockMap = new HashMap<>();
        for (ReservationStock item : stock) {
            remainingStockMap.put(item.balanceId(), item.quantity());
        }

        // Step 5: Allocate stock for each required material according to FIFO order
        List<ReservationAllocation> allocations = new ArrayList<>(); // Detail allocation from wh?, location?, lot?
        Map<UUID, BigDecimal> reservedByProduct = new HashMap<>(); // Save total success reserved per each productId
        List<ShortageDetail> shortages = new ArrayList<>(); // Save list shortages

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

            // Calculate total remaining available stock across all matching lots for this material
            BigDecimal available = stock.stream()
                    .filter(item -> item.productId().equals(material.getMaterialProductId()))
                    .map(item -> remainingStockMap.getOrDefault(item.balanceId(), BigDecimal.ZERO))
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            // Record material shortage if total available stock is less than required
            if (available.compareTo(remaining) < 0) {
                shortages.add(new ShortageDetail(material.getMaterialProductId(), remaining, available,
                        remaining.subtract(available)));
                continue;
            }

            // Deduct required quantity from FIFO stock balances
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

                // Update remaining stock tracker for this balance ID
                remainingStockMap.put(item.balanceId(), availInItem.subtract(amount));
                left = left.subtract(amount);
            }
            reservedByProduct.merge(material.getMaterialProductId(), remaining, BigDecimal::add);
        }

        UUID actorId = currentUserPort.getCurrentUserId();

        // Step 6: Handle material shortage case (update status to MATERIAL_SHORTAGE and throw exception)
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

        // Step 7: Handle successful reservation (apply stock movements, update status to READY_TO_PRODUCE)
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

    /**
     * Helper method to update the status ID of a Work Order.
     */
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

    /**
     * Helper method to validate non-null reference IDs.
     */
    private UUID requireReferenceId(UUID id, String message) {
        if (id == null) {
            throw new InvalidWorkOrderReservationException(message);
        }
        return id;
    }

    @Override @Transactional
    public void deleteWorkOrder(UUID id) {}

    @Override @Transactional(readOnly = true)
    public PageResponse<WorkOrderMaterialResponse> getMaterials(UUID workOrderId, int page, int size) {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override @Transactional
    public WorkOrderMaterialResponse addMaterial(UUID workOrderId, CreateWorkOrderMaterialRequest req) {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override @Transactional
    public void deleteMaterial(UUID workOrderId, UUID materialId) {}

    @Override @Transactional(readOnly = true)
    public PageResponse<WorkOrderEventResponse> getEvents(UUID workOrderId, int page, int size) {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override @Transactional
    public WorkOrderEventResponse addEvent(UUID workOrderId, CreateWorkOrderEventRequest req) {
        throw new UnsupportedOperationException("Not implemented");
    }
}
