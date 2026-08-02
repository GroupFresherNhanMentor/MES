package fpt.qn.mes.workorder.application.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import fpt.qn.mes.audit.domain.entities.AuditAction;
import fpt.qn.mes.audit.domain.events.AuditEvent;
import fpt.qn.mes.auth.application.port.out.CurrentUserPort;
import fpt.qn.mes.bom.domain.entities.Bom;
import fpt.qn.mes.common.port.out.JsonSerializerPort;
import fpt.qn.mes.bom.domain.repository.BomRepository;
import fpt.qn.mes.common.dto.response.PageResponse;
import fpt.qn.mes.inventory.domain.constants.MovementTypeConstants;
import fpt.qn.mes.inventory.domain.constants.StockStatusConstants;
import fpt.qn.mes.master.machine.application.port.in.MachineUseCase;
import fpt.qn.mes.master.warehouse.application.port.in.WarehouseUseCase;
import fpt.qn.mes.workorder.application.dto.request.CreateWorkOrderEventRequest;
import fpt.qn.mes.workorder.application.dto.request.CreateWorkOrderMaterialRequest;
import fpt.qn.mes.workorder.application.dto.request.CreateWorkOrderRequest;
import fpt.qn.mes.workorder.application.dto.request.StartWorkOrderRequest;
import fpt.qn.mes.workorder.application.dto.request.UpdateWorkOrderRequest;
import fpt.qn.mes.workorder.application.dto.request.WorkOrderSearchRequest;
import fpt.qn.mes.workorder.application.dto.response.ReserveWorkOrderMaterialsResponse;
import fpt.qn.mes.workorder.application.dto.response.ReservedMaterialAllocationResponse;
import fpt.qn.mes.workorder.application.dto.response.WorkOrderResponse;
import fpt.qn.mes.workorder.application.dto.response.WorkOrderEventResponse;
import fpt.qn.mes.workorder.application.dto.response.WorkOrderMaterialResponse;
import fpt.qn.mes.workorder.application.dto.workorder.complete.CompleteWorkOrderRequest;
import fpt.qn.mes.workorder.application.dto.workordereventtype.WorkOrderEventTypeResponse;
import fpt.qn.mes.workorder.application.dto.workorderpriority.WorkOrderPriorityResponse;
import fpt.qn.mes.workorder.application.dto.workorderstatus.WorkOrderStatusResponse;
import static fpt.qn.mes.workorder.application.exception.WorkOrderExceptions.*;
import fpt.qn.mes.workorder.application.mapper.WorkOrderDtoMapper;
import fpt.qn.mes.workorder.application.port.in.WorkOrderUseCase;
import fpt.qn.mes.workorder.application.port.out.AuditLogPort;
import fpt.qn.mes.workorder.application.port.out.ProductionRunPort;
import fpt.qn.mes.workorder.application.port.out.dto.ReservationAllocation;
import fpt.qn.mes.workorder.application.port.out.dto.ReservationStock;
import fpt.qn.mes.workorder.application.port.out.WorkOrderReservationPort;
import fpt.qn.mes.workorder.application.port.out.WorkOrderCompletionPort;
import fpt.qn.mes.workorder.application.port.out.dto.ActiveProductionRun;
import fpt.qn.mes.workorder.application.port.out.dto.CompletionReferences;
import fpt.qn.mes.workorder.application.port.out.dto.CompletionReservationAllocation;
import fpt.qn.mes.workorder.domain.constants.WorkOrderStatusConstants;
import fpt.qn.mes.workorder.domain.entities.WorkOrder;
import fpt.qn.mes.workorder.domain.entities.WorkOrderMaterial;
import fpt.qn.mes.workorder.domain.repository.WorkOrderRepository;
import fpt.qn.mes.workorder.domain.repository.criteria.WorkOrderSearchCriteria;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import org.springframework.context.ApplicationEventPublisher;

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
    WorkOrderCompletionPort completionPort;
    ProductionRunPort productionRunPort;
    AuditLogPort auditLogPort;
    CurrentUserPort currentUserPort;
    ApplicationEventPublisher eventPublisher;
    JsonSerializerPort jsonSerializer;

    @NonFinal
    @Value("${app.inventory.raw-material-warehouse-code:RAW_MATERIAL_WAREHOUSE}")
    String rawMaterialWarehouseCode;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<WorkOrderResponse> getWorkOrders(WorkOrderSearchRequest request) {
        WorkOrderSearchCriteria criteria = WorkOrderSearchCriteria.builder()
                .page(request != null ? request.getPage() : 0)
                .size(request != null ? request.getSize() : 20)
                .finishedProductId(request != null ? request.getFinishedProductId() : null)
                .statusId(request != null ? request.getStatusId() : null)
                .code(request != null ? request.getCode() : null)
                .build();

        var result = repository.findAll(criteria);
        var dtos = result.getItems().stream()
                .map(workOrder -> mapper.toDto(workOrder))
                .toList();

        return PageResponse.<WorkOrderResponse>of(dtos, result.getTotal(), criteria.getPage(), criteria.getSize());
    }

    @Override
    @Transactional(readOnly = true)
    public WorkOrderResponse getWorkOrderById(UUID id) {
        var workOrder = repository.findById(id)
                .orElseThrow(() -> new WorkOrderNotFoundException("Work Order not found with ID: " + id));

        var materials = repository.findMaterialsByWorkOrderId(id).stream()
                .map(material -> mapper.toDto(material))
                .toList();

        var events = repository.findEventsByWorkOrderId(id).stream()
                .map(event -> mapper.toDto(event))
                .toList();

        WorkOrderResponse baseDto = mapper.toDto(workOrder);

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

    @Override
    @Transactional
    public WorkOrderResponse createWorkOrder(CreateWorkOrderRequest req, UUID currentUserId) {
        if (req.getPlannedQuantity().compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidInputException("Planned quantity must be greater than 0");
        }
        if (req.getPlannedStartDate() != null && req.getPlannedEndDate() != null
                && !req.getPlannedStartDate().isBefore(req.getPlannedEndDate())) {
            throw new InvalidInputException("Planned start date must be before planned end date");
        }
        if (repository.existsByCode(req.getCode())) {
            throw new WorkOrderCodeExistsException("Work Order code '" + req.getCode() + "' already exists");
        }
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
                .workOrderStatusId(req.getWorkOrderStatusId() != null ? req.getWorkOrderStatusId()
                        : repository.findStatusIdByName(WorkOrderStatusConstants.DRAFT)
                                .orElseThrow(() -> new InvalidWorkOrderStateException("DRAFT Work Order status is not configured")))
                .createdBy(currentUserId)
                .createdAt(Instant.now())
                .build();

        WorkOrder saved = repository.save(workOrder);
        eventPublisher.publishEvent(AuditEvent.create(currentUserId, AuditAction.CREATE_WORK_ORDER,
                "WORK_ORDER", saved.getId(), null, jsonSerializer.toJson(saved), null));

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
    @Transactional(readOnly = true)
    public List<WorkOrderStatusResponse> getWorkOrderStatuses() {
        return repository.findAllStatuses().stream()
                .map(s -> mapper.toDto(s))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<WorkOrderPriorityResponse> getWorkOrderPriorities() {
        return repository.findAllPriorities().stream()
                .map(p -> mapper.toDto(p))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<WorkOrderEventTypeResponse> getWorkOrderEventTypes() {
        return repository.findAllEventTypes().stream()
                .map(e -> mapper.toDto(e))
                .toList();
    }

    @Override
    @Transactional
    public WorkOrderResponse updateWorkOrder(UUID id, UpdateWorkOrderRequest req) {
        if (req == null) {
            throw new InvalidInputException("Update request body cannot be null");
        }

        WorkOrder workOrder = repository.findForUpdate(id)
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
            String targetStatusName = repository.findStatusNameById(req.getWorkOrderStatusId())
                    .orElseThrow(() -> new InvalidWorkOrderStateException("Target Work Order status is not configured"));
            if (!WorkOrderStatusConstants.DRAFT.equals(targetStatusName) && !WorkOrderStatusConstants.PLANNED.equals(targetStatusName)) {
                throw new InvalidWorkOrderStateException(
                        "PUT endpoint only supports DRAFT ⇄ PLANNED transitions. Use dedicated action endpoints for other state changes.");
            }
            requireActiveTransition(workOrder, currentStatusName, targetStatusName, targetStatusId);
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
        UUID actorId = currentUserPort.getCurrentUserId();
        eventPublisher.publishEvent(AuditEvent.create(actorId, AuditAction.UPDATE_WORK_ORDER,
                "WORK_ORDER", id, jsonSerializer.toJson(workOrder), jsonSerializer.toJson(saved), null));

        if (quantityChanged) {
            var existingMaterials = repository.findMaterialsByWorkOrderId(id);
            var bomOptional = bomRepository.findActiveByFinishedProductId(saved.getFinishedProductId());
            if (bomOptional.isPresent()) {
                var bom = bomOptional.get();
                if (bom.getItems() != null && !bom.getItems().isEmpty()) {
                    Map<UUID, BigDecimal> materialFactorByProduct = new HashMap<>();
                    for (var item : bom.getItems()) {
                        BigDecimal scrap = item.getScrapRate() != null ? item.getScrapRate() : BigDecimal.ZERO;
                        materialFactorByProduct.put(item.getMaterialProductId(),
                                item.getQuantityPerUnit().multiply(BigDecimal.ONE.add(scrap)));
                    }
                    for (var mat : existingMaterials) {
                        BigDecimal materialFactor = materialFactorByProduct.get(mat.getMaterialProductId());
                        if (materialFactor != null) {
                            BigDecimal reqQty = saved.getPlannedQuantity().multiply(materialFactor);

                            WorkOrderMaterial updatedMat = WorkOrderMaterial.builder()
                                    .id(mat.getId())
                                    .workOrderId(mat.getWorkOrderId())
                                    .materialProductId(mat.getMaterialProductId())
                                    .requiredQuantity(reqQty)
                                    .reservedQuantity(mat.getReservedQuantity())
                                    .consumedQuantity(mat.getConsumedQuantity())
                                    .build();
                            repository.updateMaterial(updatedMat);
                        }
                    }
                }
            }
        }

        return getWorkOrderById(saved.getId());
    }

    @Override
    @Transactional(noRollbackFor = InsufficientMaterialException.class)
    public ReserveWorkOrderMaterialsResponse reserveMaterials(UUID workOrderId) {
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
        Map<UUID, List<ReservationStock>> stockByProduct = new HashMap<>();
        for (ReservationStock item : stock) {
            remainingStockMap.put(item.balanceId(), item.quantity());
            stockByProduct.computeIfAbsent(item.productId(), ignored -> new ArrayList<>()).add(item);
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

            List<ReservationStock> productStock = stockByProduct.getOrDefault(
                    material.getMaterialProductId(), List.of());
            BigDecimal available = productStock.stream()
                    .map(item -> remainingStockMap.getOrDefault(item.balanceId(), BigDecimal.ZERO))
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            if (available.compareTo(remaining) < 0) {
                shortages.add(new ShortageDetail(material.getMaterialProductId(), remaining, available,
                        remaining.subtract(available)));
                continue;
            }

            BigDecimal left = remaining;
            for (ReservationStock item : productStock) {
                if (left.compareTo(BigDecimal.ZERO) <= 0) {
                    break;
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
            if (!WorkOrderStatusConstants.MATERIAL_SHORTAGE.equals(currentStatus)) {
                UUID shortageStatusId = requireActiveTransition(workOrder, currentStatus,
                        WorkOrderStatusConstants.MATERIAL_SHORTAGE);
                updateStatus(workOrder, shortageStatusId);
                auditLogPort.recordStatusTransition(actorId, workOrderId, currentStatus,
                        WorkOrderStatusConstants.MATERIAL_SHORTAGE, "RESERVE_MATERIAL");
            }
            throw new InsufficientMaterialException(
                    "Insufficient stock for one or more Work Order materials", shortages);
        }

        UUID readyStatusId = requireActiveTransition(workOrder, currentStatus,
                WorkOrderStatusConstants.READY_TO_PRODUCE);
        reservationPort.applyReservation(workOrderId, allocations, reservedByProduct, availableStatusId,
                reservedStatusId, movementTypeId, actorId);
        updateStatus(workOrder, readyStatusId);
        auditLogPort.recordStatusTransition(actorId, workOrderId, currentStatus,
                WorkOrderStatusConstants.READY_TO_PRODUCE, "RESERVE_MATERIAL");
        return ReserveWorkOrderMaterialsResponse.builder()
                .workOrderId(workOrderId)
                .status(WorkOrderStatusConstants.READY_TO_PRODUCE)
                .allocations(allocations.stream().map(allocation -> ReservedMaterialAllocationResponse.builder()
                        .materialProductId(allocation.productId())
                        .lotId(allocation.lotId())
                        .warehouseId(allocation.warehouseId())
                        .locationId(allocation.locationId())
                        .reservedQuantity(allocation.quantity())
                        .build()).toList())
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

    private UUID requireActiveTransition(WorkOrder workOrder, String currentStatus, String targetStatus) {
        UUID targetStatusId = repository.findStatusIdByName(targetStatus)
                .orElseThrow(() -> new InvalidWorkOrderStateException(targetStatus + " status is not configured"));
        requireActiveTransition(workOrder, currentStatus, targetStatus, targetStatusId);
        return targetStatusId;
    }

    private void requireActiveTransition(WorkOrder workOrder, String currentStatus, String targetStatus,
            UUID targetStatusId) {
        if (!repository.hasActiveTransition(workOrder.getWorkOrderStatusId(), targetStatusId)) {
            throw new InvalidWorkOrderStateException(
                    currentStatus + " to " + targetStatus + " transition is not active");
        }
    }

    private UUID requireReferenceId(UUID id, String message) {
        if (id == null) {
            throw new InvalidWorkOrderReservationException(message);
        }
        return id;
    }

    @Override
    @Transactional
    public WorkOrderResponse releaseMaterials(UUID workOrderId) {
        // Serialize state transitions for this work order before touching its reservations.
        WorkOrder workOrder = repository.findForUpdate(workOrderId)
                .orElseThrow(() -> new WorkOrderNotFoundException("Work Order not found with ID: " + workOrderId));

        String currentStatus = repository.findStatusNameById(workOrder.getWorkOrderStatusId()).orElse("");
        if (!WorkOrderStatusConstants.READY_TO_PRODUCE.equals(currentStatus)
                && !WorkOrderStatusConstants.PLANNED.equals(currentStatus)) {
            throw new InvalidWorkOrderStateException(
                    "Cannot release materials for work order in " + currentStatus + " status");
        }

        UUID plannedStatusId = WorkOrderStatusConstants.READY_TO_PRODUCE.equals(currentStatus)
                ? requireActiveTransition(workOrder, currentStatus, WorkOrderStatusConstants.PLANNED)
                : null;

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
            updateStatus(workOrder, plannedStatusId);
            auditLogPort.recordStatusTransition(actorId, workOrderId, currentStatus,
                    WorkOrderStatusConstants.PLANNED, "RELEASE_RESERVATION");
        }

        return getWorkOrderById(workOrderId);
    }

    @Override
    @Transactional
    public WorkOrderResponse cancelWorkOrder(UUID workOrderId) {
        // Lock first so cancellation cannot race a release or production transition.
        WorkOrder workOrder = repository.findForUpdate(workOrderId)
                .orElseThrow(() -> new WorkOrderNotFoundException("Work Order not found with ID: " + workOrderId));

        String currentStatus = repository.findStatusNameById(workOrder.getWorkOrderStatusId()).orElse("");
        UUID cancelledStatusId = requireActiveTransition(workOrder, currentStatus,
                WorkOrderStatusConstants.CANCELLED);

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
        updateStatus(workOrder, cancelledStatusId);
        auditLogPort.recordStatusTransition(actorId, workOrderId, currentStatus,
                WorkOrderStatusConstants.CANCELLED, "RELEASE_RESERVATION");

        return getWorkOrderById(workOrderId);
    }

    @Override
    @Transactional
    public WorkOrderResponse startWorkOrder(UUID workOrderId, StartWorkOrderRequest request) {
        if (request == null || request.getMachineId() == null) {
            throw new InvalidInputException("machineId is required to start production");
        }

        WorkOrder workOrder = repository.findForUpdate(workOrderId)
                .orElseThrow(() -> new WorkOrderNotFoundException("Work Order not found with ID: " + workOrderId));
        String currentStatus = repository.findStatusNameById(workOrder.getWorkOrderStatusId()).orElse("");
        if (!WorkOrderStatusConstants.READY_TO_PRODUCE.equals(currentStatus)) {
            throw new InvalidWorkOrderStateException(
                    "Work Order must be in READY_TO_PRODUCE status to start production");
        }
        UUID inProgressStatusId = requireActiveTransition(workOrder, currentStatus,
                WorkOrderStatusConstants.IN_PROGRESS);
        if (!productionRunPort.lockMachine(request.getMachineId())) {
            throw new MachineNotAvailableException("Machine not found: " + request.getMachineId());
        }
        if (!machineUseCase.isAvailableForReservation(request.getMachineId())) {
            throw new MachineNotAvailableException("Machine is not AVAILABLE for production");
        }
        if (productionRunPort.isMachineRunning(request.getMachineId())) {
            throw new InvalidWorkOrderStateException("Machine is currently running another work order");
        }

        UUID actorId = currentUserPort.getCurrentUserId();
        UUID operatorId = request.getOperatorId() != null ? request.getOperatorId() : actorId;
        UUID runId = productionRunPort.createProductionRun(
                workOrderId, request.getMachineId(), request.getProductionLineId(), operatorId);
        productionRunPort.updateMachineStatus(request.getMachineId(), "RUNNING");

        updateStatus(workOrder, inProgressStatusId);
        productionRunPort.recordWorkOrderEvent(workOrderId, runId, "START", operatorId);
        auditLogPort.recordStatusTransition(actorId, workOrderId, currentStatus,
                WorkOrderStatusConstants.IN_PROGRESS, "START_PRODUCTION");
        return getWorkOrderById(workOrderId);
    }

    @Override
    @Transactional
    public WorkOrderResponse pauseWorkOrder(UUID workOrderId) {
        WorkOrder workOrder = repository.findForUpdate(workOrderId)
                .orElseThrow(() -> new WorkOrderNotFoundException("Work Order not found with ID: " + workOrderId));
        String currentStatus = repository.findStatusNameById(workOrder.getWorkOrderStatusId()).orElse("");
        if (!WorkOrderStatusConstants.IN_PROGRESS.equals(currentStatus)) {
            throw new InvalidWorkOrderStateException("Work Order must be IN_PROGRESS to pause");
        }

        UUID actorId = currentUserPort.getCurrentUserId();
        UUID runId = productionRunPort.findActiveProductionRunId(workOrderId)
                .orElseThrow(() -> new InvalidWorkOrderStateException("Work Order has no active production run"));
        UUID pausedStatusId = requireActiveTransition(workOrder, currentStatus, WorkOrderStatusConstants.PAUSED);
        updateStatus(workOrder, pausedStatusId);
        productionRunPort.recordWorkOrderEvent(workOrderId, runId, "PAUSE", actorId);
        auditLogPort.recordStatusTransition(actorId, workOrderId, currentStatus,
                WorkOrderStatusConstants.PAUSED, "PAUSE_PRODUCTION");
        return getWorkOrderById(workOrderId);
    }

    @Override
    @Transactional
    public WorkOrderResponse resumeWorkOrder(UUID workOrderId) {
        WorkOrder workOrder = repository.findForUpdate(workOrderId)
                .orElseThrow(() -> new WorkOrderNotFoundException("Work Order not found with ID: " + workOrderId));
        String currentStatus = repository.findStatusNameById(workOrder.getWorkOrderStatusId()).orElse("");
        if (!WorkOrderStatusConstants.PAUSED.equals(currentStatus)) {
            throw new InvalidWorkOrderStateException("Work Order must be PAUSED to resume");
        }

        UUID actorId = currentUserPort.getCurrentUserId();
        UUID runId = productionRunPort.findActiveProductionRunId(workOrderId)
                .orElseThrow(() -> new InvalidWorkOrderStateException("Work Order has no active production run"));
        UUID inProgressStatusId = requireActiveTransition(workOrder, currentStatus,
                WorkOrderStatusConstants.IN_PROGRESS);
        updateStatus(workOrder, inProgressStatusId);
        productionRunPort.recordWorkOrderEvent(workOrderId, runId, "RESUME", actorId);
        auditLogPort.recordStatusTransition(actorId, workOrderId, currentStatus,
                WorkOrderStatusConstants.IN_PROGRESS, "RESUME_PRODUCTION");
        return getWorkOrderById(workOrderId);
    }

    @Override
    @Transactional
    public WorkOrderResponse completeWorkOrder(UUID workOrderId, CompleteWorkOrderRequest request) {
        // Validate the reported production split before any persistent state is locked or changed.
        if (request == null) {
            throw new InvalidInputException("Completion request body cannot be null");
        }
        BigDecimal reportedTotal = request.getGoodQuantity().add(request.getDefectQuantity()).add(request.getScrapQuantity());
        if (reportedTotal.compareTo(request.getActualQuantity()) != 0) {
            throw new InvalidInputException("Good, defect, and scrap quantities must equal actual quantity");
        }
        // Lock the order first so a competing completion cannot pass the lifecycle check.
        WorkOrder workOrder = repository.findForUpdate(workOrderId)
                .orElseThrow(() -> new WorkOrderNotFoundException("Work Order not found with ID: " + workOrderId));
        String currentStatus = repository.findStatusNameById(workOrder.getWorkOrderStatusId()).orElse("");
        if (!WorkOrderStatusConstants.IN_PROGRESS.equals(currentStatus)) {
            throw new InvalidWorkOrderStateException("Work Order must be IN_PROGRESS to complete");
        }
        // The configured transition table, rather than a hard-coded rule alone, authorizes finalization.
        UUID completedStatusId = requireActiveTransition(workOrder, currentStatus,
                WorkOrderStatusConstants.COMPLETED);
        // The active run identifies the machine that must be released and is locked for a single close.
        ActiveProductionRun productionRun = productionRunPort.findActiveProductionRunForUpdate(workOrderId)
                .orElseThrow(() -> new InvalidWorkOrderStateException("Work Order has no active production run"));
        var destination = completionPort.findOutputDestination(request.getOutputWarehouseId(), request.getOutputLocationId())
                .orElseThrow(() -> new InvalidInputException("Output location must belong to the output warehouse"));
        CompletionReferences references = completionPort.findCompletionReferences();
        if (!hasAllCompletionReferences(references)) {
            throw new InvalidWorkOrderStateException("Completion reference data is not configured");
        }
        // Build lot-level consumption, scrap, and release commands from the immutable reservation ledger.
        List<CompletionReservationAllocation> allocations = calculateCompletionAllocations(
                completionPort.findOutstandingReservations(workOrderId), workOrder.getPlannedQuantity(), request);
        UUID actorId = currentUserPort.getCurrentUserId();
        // Persist all stock, output, run, machine, and event effects before exposing the completed state.
        if (!completionPort.finalizeCompletion(workOrder, productionRun, request, allocations, destination, references, actorId)) {
            throw new InvalidWorkOrderStateException("Reserved material balance or production run is inconsistent");
        }
        // The status and audit record are written last inside this transaction to preserve all-or-nothing completion.
        updateStatus(workOrder, completedStatusId);
        auditLogPort.recordCompletion(actorId, workOrderId);
        return getWorkOrderById(workOrderId);
    }

    private List<CompletionReservationAllocation> calculateCompletionAllocations(
            List<CompletionReservationAllocation> reservations, BigDecimal plannedQuantity,
            CompleteWorkOrderRequest request) {
        // Aggregate reservations first because one material can be reserved from several physical lots.
        Map<UUID, BigDecimal> reservedByProduct = new HashMap<>();
        for (CompletionReservationAllocation reservation : reservations) {
            reservedByProduct.merge(reservation.getMaterialProductId(), reservation.getReservedQuantity(),
                    (left, right) -> left.add(right));
        }
        Map<UUID, BigDecimal> remainingConsumption = new HashMap<>();
        for (var entry : reservedByProduct.entrySet()) {
            // Consumption scales with actual output but can never exceed the material reserved for this order.
            BigDecimal consumed = entry.getValue().multiply(request.getActualQuantity())
                    .divide(plannedQuantity, 4, RoundingMode.HALF_UP).min(entry.getValue());
            remainingConsumption.put(entry.getKey(), consumed);
        }
        List<CompletionReservationAllocation> allocations = new ArrayList<>();
        for (CompletionReservationAllocation reservation : reservations) {
            // Allocate the capped material consumption in deterministic reservation order.
            BigDecimal remaining = remainingConsumption.getOrDefault(reservation.getMaterialProductId(), BigDecimal.ZERO);
            BigDecimal consumed = reservation.getReservedQuantity().min(remaining);
            // Split each consumed lot between normal production and material loss without dividing by zero.
            BigDecimal scrap = request.getActualQuantity().compareTo(BigDecimal.ZERO) == 0
                    ? BigDecimal.ZERO
                    : consumed.multiply(request.getScrapQuantity()).divide(request.getActualQuantity(), 4, RoundingMode.HALF_UP);
            allocations.add(CompletionReservationAllocation.builder()
                    .materialProductId(reservation.getMaterialProductId())
                    .warehouseId(reservation.getWarehouseId())
                    .locationId(reservation.getLocationId())
                    .lotId(reservation.getLotId())
                    .reservedQuantity(reservation.getReservedQuantity())
                    .consumedQuantity(consumed)
                    .scrapQuantity(scrap)
                    .build());
            remainingConsumption.put(reservation.getMaterialProductId(), remaining.subtract(consumed));
        }
        return allocations;
    }

    private boolean hasAllCompletionReferences(CompletionReferences references) {
        return references != null && references.getAvailableStatusId() != null && references.getReservedStatusId() != null
                && references.getConsumedStatusId() != null && references.getScrappedStatusId() != null
                && references.getQualityInspectionStatusId() != null && references.getConsumeMovementTypeId() != null
                && references.getScrapMovementTypeId() != null && references.getReleaseMovementTypeId() != null
                && references.getProductionOutputMovementTypeId() != null && references.getProductionLotTypeId() != null
                && references.getPendingInspectionStatusId() != null && references.getCompleteEventTypeId() != null
                && references.getAvailableMachineStatusId() != null;
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
