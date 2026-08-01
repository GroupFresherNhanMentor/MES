package fpt.qn.mes.inventory.application.service;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import fpt.qn.mes.audit.domain.entities.AuditAction;
import fpt.qn.mes.audit.domain.events.AuditEvent;
import fpt.qn.mes.auth.application.port.out.CurrentUserPort;
import fpt.qn.mes.common.dto.response.PageResponse;
import fpt.qn.mes.common.util.UuidV7;
import fpt.qn.mes.inventory.application.dto.response.StockTransferResponse;
import fpt.qn.mes.inventory.application.dto.stockadjustment.create.CreateStockAdjustmentRequest;
import fpt.qn.mes.inventory.application.dto.stockadjustmentapproval.StockAdjustmentApprovalResponse;
import fpt.qn.mes.inventory.application.dto.stockbalance.StockBalanceResponse;
import fpt.qn.mes.inventory.application.dto.stockbalance.search.StockBalanceSearchRequest;
import fpt.qn.mes.inventory.application.dto.stockmovement.StockMovementResponse;
import fpt.qn.mes.inventory.application.dto.stockmovement.create.CreateStockMovementRequest;
import fpt.qn.mes.inventory.application.dto.stockmovement.create.StockInRequest;
import fpt.qn.mes.inventory.application.dto.stockmovement.create.StockTransferRequest;
import fpt.qn.mes.inventory.application.dto.stockmovement.search.StockMovementSearchRequest;
import fpt.qn.mes.inventory.application.exception.InsufficientStockException;
import fpt.qn.mes.inventory.application.exception.InvalidStockAdjustmentException;
import fpt.qn.mes.inventory.application.exception.InvalidStockLotException;
import fpt.qn.mes.inventory.application.exception.InventoryNotFoundException;
import fpt.qn.mes.inventory.application.mapper.InventoryDtoMapper;
import fpt.qn.mes.inventory.application.mapper.StockAdjustmentApprovalDtoMapper;
import fpt.qn.mes.inventory.application.port.in.InventoryUseCase;
import fpt.qn.mes.inventory.application.port.out.ProductCheckPort;
import fpt.qn.mes.inventory.application.port.out.WarehouseCheckPort;
import fpt.qn.mes.inventory.application.port.out.WarehouseLocationCheckPort;
import fpt.qn.mes.inventory.application.port.out.WarehouseLocationQueryPort;
import fpt.qn.mes.inventory.domain.constants.MovementTypeConstants;
import fpt.qn.mes.inventory.domain.constants.StockStatusConstants;
import fpt.qn.mes.inventory.domain.entities.StockAdjustmentApproval;
import fpt.qn.mes.inventory.domain.entities.StockBalance;
import fpt.qn.mes.inventory.domain.entities.StockLot;
import fpt.qn.mes.inventory.domain.entities.StockMovement;
import fpt.qn.mes.inventory.domain.repository.MovementTypeRepository;
import fpt.qn.mes.inventory.domain.repository.StockAdjustmentApprovalRepository;
import fpt.qn.mes.inventory.domain.repository.StockBalanceRepository;
import fpt.qn.mes.inventory.domain.repository.StockLotRepository;
import fpt.qn.mes.inventory.domain.repository.StockMovementRepository;
import fpt.qn.mes.inventory.domain.repository.StockStatusRepository;
import fpt.qn.mes.inventory.domain.repository.criteria.StockAdjustmentApprovalSearchCriteria;
import fpt.qn.mes.inventory.domain.repository.criteria.StockBalanceSearchCriteria;
import fpt.qn.mes.inventory.domain.repository.criteria.StockMovementSearchCriteria;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class InventoryService implements InventoryUseCase {

    StockLotRepository lotRepository;
    StockMovementRepository movementRepository;
    StockBalanceRepository balanceRepository;
    MovementTypeRepository movementTypeRepository;
    StockStatusRepository stockStatusRepository;
    StockAdjustmentApprovalRepository approvalRepository;
    InventoryDtoMapper mapper;
    StockAdjustmentApprovalDtoMapper approvalMapper;
    CurrentUserPort currentUserPort;
    ProductCheckPort productCheckPort;
    WarehouseCheckPort warehouseCheckPort;
    WarehouseLocationCheckPort warehouseLocationCheckPort;
    WarehouseLocationQueryPort warehouseLocationQueryPort;
    ApplicationEventPublisher eventPublisher;


    private static final BigDecimal ADJUSTMENT_THRESHOLD = new BigDecimal("100.00");

    @Override
    @Transactional(readOnly = true)
    public PageResponse<StockMovementResponse> getMovements(StockMovementSearchRequest request) {
        StockMovementSearchCriteria criteria = StockMovementSearchCriteria.builder()
                .movementTypeId(request != null ? request.getMovementTypeId() : null)
                .productId(request != null ? request.getProductId() : null)
                .lotId(request != null ? request.getLotId() : null)
                .warehouseId(request != null ? request.getWarehouseId() : null)
                .locationId(request != null ? request.getLocationId() : null)
                .referenceNo(request != null ? request.getReferenceNo() : null)
                .page(request != null ? request.getPage() : 0)
                .size(request != null ? request.getSize() : 20)
                .sort(request != null && request.getSort() != null ? request.getSort() : List.of())
                .build();

        long total = movementRepository.count(criteria);
        List<StockMovementResponse> dtos = movementRepository.search(criteria).stream()
                .map(m -> mapper.toDto(m))
                .collect(Collectors.toList());
        return PageResponse.of(dtos, total, request != null ? request.getPage() : 0, request != null ? request.getSize() : 20);
    }

    @Override
    @Transactional
    public void recordMovement(CreateStockMovementRequest request) {
        if (request.getQuantity() == null || request.getQuantity().compareTo(BigDecimal.ZERO) <= 0) {
            throw new InsufficientStockException("Movement quantity must be positive");
        }
        if (!productCheckPort.existsById(request.getProductId())) {
            throw new InventoryNotFoundException("Product not found: " + request.getProductId());
        }
        if (!warehouseCheckPort.existsById(request.getWarehouseId())) {
            throw new InventoryNotFoundException("Warehouse not found: " + request.getWarehouseId());
        }
        if (request.getLocationId() != null && !warehouseLocationCheckPort.existsById(request.getLocationId())) {
            throw new InventoryNotFoundException("Location not found: " + request.getLocationId());
        }

        UUID currentUserId = currentUserPort.getCurrentUserId();

        if (request.getFromStatusId() != null) {
            StockBalance fromBalance = balanceRepository.findForUpdate(
                    request.getWarehouseId(), request.getLocationId(),
                    request.getProductId(), request.getLotId(), request.getFromStatusId()
            ).orElseThrow(() -> new InsufficientStockException("Insufficient stock balance for this movement"));
            fromBalance.deductQuantity(request.getQuantity());
            balanceRepository.save(fromBalance);
        }

        if (request.getToStatusId() != null) {
            Optional<StockBalance> optToBalance = balanceRepository.findForUpdate(
                    request.getWarehouseId(), request.getLocationId(),
                    request.getProductId(), request.getLotId(), request.getToStatusId()
            );
            StockBalance toBalance = optToBalance.map(b -> {
                b.addQuantity(request.getQuantity());
                return b;
            }).orElse(StockBalance.builder()
                    .id(UuidV7.generate())
                    .warehouseId(request.getWarehouseId())
                    .locationId(request.getLocationId())
                    .productId(request.getProductId())
                    .lotId(request.getLotId())
                    .stockStatusId(request.getToStatusId())
                    .quantity(request.getQuantity())
                    .version(1L)
                    .createdAt(Instant.now())
                    .updatedAt(Instant.now())
                    .build());
            balanceRepository.save(toBalance);
        }

        UUID fromWh = request.getFromStatusId() != null ? request.getWarehouseId() : null;
        UUID fromLoc = request.getFromStatusId() != null ? request.getLocationId() : null;
        UUID toWh = (request.getToStatusId() != null || request.getFromStatusId() == null) ? request.getWarehouseId() : null;
        UUID toLoc = (request.getToStatusId() != null || request.getFromStatusId() == null) ? request.getLocationId() : null;

        movementRepository.save(StockMovement.create(
                request.getMovementTypeId(), request.getProductId(), request.getLotId(),
                fromWh, fromLoc, toWh, toLoc, request.getQuantity(),
                request.getFromStatusId(), request.getToStatusId(),
                request.getReferenceNo(), request.getReason(), currentUserId));
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<StockBalanceResponse> getStockBalances(StockBalanceSearchRequest request) {
        StockBalanceSearchCriteria criteria = StockBalanceSearchCriteria.builder()
                .warehouseId(request != null ? request.getWarehouseId() : null)
                .locationId(request != null ? request.getLocationId() : null)
                .productId(request != null ? request.getProductId() : null)
                .lotId(request != null ? request.getLotId() : null)
                .stockStatusId(request != null ? request.getStockStatusId() : null)
                .page(request != null ? request.getPage() : 0)
                .size(request != null ? request.getSize() : 20)
                .sort(request != null && request.getSort() != null ? request.getSort() : List.of())
                .build();
        long total = balanceRepository.count(criteria);
        List<StockBalanceResponse> dtos = balanceRepository.search(criteria).stream()
                .map(b -> mapper.toDto(b))
                .collect(Collectors.toList());
        return PageResponse.of(dtos, total, request != null ? request.getPage() : 0, request != null ? request.getSize() : 20);
    }

    @Override
    @Transactional
    public void recordStockIn(StockInRequest request) {
        if (!productCheckPort.existsById(request.getProductId())) {
            throw new InventoryNotFoundException("Product not found: " + request.getProductId());
        }
        if (!warehouseCheckPort.existsById(request.getWarehouseId())) {
            throw new InventoryNotFoundException("Warehouse not found: " + request.getWarehouseId());
        }
        if (!warehouseLocationCheckPort.existsById(request.getLocationId())) {
            throw new InventoryNotFoundException("Location not found: " + request.getLocationId());
        }

        UUID currentUserId = currentUserPort.getCurrentUserId();

        StockLot lot;
        Optional<StockLot> optExistingLot = lotRepository.findByLotNumber(request.getLotNumber());
        if (optExistingLot.isPresent()) {
            lot = optExistingLot.get();
            if (!request.getProductId().equals(lot.getProductId())) {
                throw new InvalidStockLotException(
                        "Stock lot '" + request.getLotNumber() + "' belongs to a different product");
            }
        } else {
            lot = lotRepository.save(StockLot.create(
                    request.getLotNumber(), request.getProductId(),
                    request.getLotTypeId(), request.getExpiryDate(), currentUserId));
        }

        UUID statusId = stockStatusRepository.findIdByName(StockStatusConstants.AVAILABLE)
                .orElseThrow(() -> new InventoryNotFoundException("Stock status AVAILABLE not found"));
        UUID purchaseInTypeId = movementTypeRepository.findIdByName(MovementTypeConstants.PURCHASE_IN)
                .orElseThrow(() -> new InventoryNotFoundException("Movement type PURCHASE_IN not found"));

        Optional<StockBalance> optBalance = balanceRepository.findForUpdate(
                request.getWarehouseId(), request.getLocationId(),
                request.getProductId(), lot.getId(), statusId);

        BigDecimal newQty = optBalance.map(b -> b.getQuantity()).orElse(BigDecimal.ZERO).add(request.getQuantity());

        balanceRepository.save(StockBalance.builder()
                .id(optBalance.map(b -> b.getId()).orElse(UuidV7.generate()))
                .warehouseId(request.getWarehouseId())
                .locationId(request.getLocationId())
                .productId(request.getProductId())
                .lotId(lot.getId())
                .stockStatusId(statusId)
                .quantity(newQty)
                .version(optBalance.map(b -> b.getVersion() == null ? 1L : b.getVersion() + 1).orElse(1L))
                .createdAt(optBalance.map(b -> b.getCreatedAt()).orElse(Instant.now()))
                .updatedAt(Instant.now())
                .build());

        movementRepository.save(StockMovement.create(
                purchaseInTypeId, request.getProductId(), lot.getId(),
                null, null, request.getWarehouseId(), request.getLocationId(),
                request.getQuantity(), null, statusId,
                request.getReferenceNo(), request.getReason(), currentUserId));
    }

    @Override
    @Transactional
    public void adjustStock(CreateStockAdjustmentRequest request) {
        if (request == null) throw new InvalidStockAdjustmentException("Request body is required");
        if (request.getReason() == null || request.getReason().isBlank())
            throw new InvalidStockAdjustmentException("Reason is required for stock adjustment");
        if (request.getStockBalanceId() == null)
            throw new InvalidStockAdjustmentException("Stock balance ID is required");
        if (request.getQuantityAdjustment() == null)
            throw new InvalidStockAdjustmentException("Quantity adjustment is required");

        StockBalance balance = balanceRepository.findById(request.getStockBalanceId())
                .orElseThrow(() -> new InventoryNotFoundException("Stock balance not found: " + request.getStockBalanceId()));

        BigDecimal result = balance.getQuantity().add(request.getQuantityAdjustment());
        if (result.compareTo(BigDecimal.ZERO) < 0)
            throw new InvalidStockAdjustmentException("Adjustment cannot result in negative stock balance");

        UUID currentUserId = currentUserPort.getCurrentUserId();

        if (request.getQuantityAdjustment().abs().compareTo(ADJUSTMENT_THRESHOLD) > 0) {
            approvalRepository.save(StockAdjustmentApproval.create(
                    balance.getProductId(), balance.getWarehouseId(), balance.getLocationId(),
                    balance.getId(), request.getQuantityAdjustment(),
                    request.getReason(), request.getReferenceNo(), currentUserId));
            return;
        }

        balanceRepository.save(StockBalance.builder()
                .id(balance.getId())
                .warehouseId(balance.getWarehouseId()).locationId(balance.getLocationId())
                .productId(balance.getProductId()).lotId(balance.getLotId())
                .stockStatusId(balance.getStockStatusId()).quantity(result)
                .version(balance.getVersion() == null ? 1L : balance.getVersion() + 1)
                .createdAt(balance.getCreatedAt()).updatedAt(Instant.now())
                .build());

        UUID adjustmentTypeId = movementTypeRepository.findIdByName(MovementTypeConstants.ADJUSTMENT)
                .orElseThrow(() -> new InventoryNotFoundException("Movement type ADJUSTMENT not found"));

        BigDecimal abs = request.getQuantityAdjustment().abs();
        boolean positive = request.getQuantityAdjustment().compareTo(BigDecimal.ZERO) > 0;
        movementRepository.save(StockMovement.create(
                adjustmentTypeId, balance.getProductId(), balance.getLotId(),
                positive ? null : balance.getWarehouseId(),
                positive ? null : balance.getLocationId(),
                positive ? balance.getWarehouseId() : null,
                positive ? balance.getLocationId() : null,
                abs, balance.getStockStatusId(), balance.getStockStatusId(),
                request.getReferenceNo(), request.getReason(), currentUserId));
        if (eventPublisher != null) {
            eventPublisher.publishEvent(AuditEvent.create(currentUserId, AuditAction.ADJUST_STOCK, "STOCK_BALANCE", balance.getId(),
                    "{\"quantity\":" + balance.getQuantity() + "}",
                    "{\"quantity\":" + result + "}", null));
        }
        return;
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<StockAdjustmentApprovalResponse> getPendingAdjustments(StockAdjustmentApprovalSearchCriteria criteria) {
        if (criteria == null) criteria = StockAdjustmentApprovalSearchCriteria.builder().page(0).size(20).build();
        long total = approvalRepository.count(criteria);
        List<StockAdjustmentApprovalResponse> dtos = approvalRepository.search(criteria).stream()
                .map(a -> approvalMapper.toDto(a))
                .collect(Collectors.toList());
        return PageResponse.of(dtos, total, criteria.getPage(), criteria.getSize());
    }

    @Override
    @Transactional
    public StockMovementResponse approveAdjustment(UUID approvalId) {
        StockAdjustmentApproval approval = approvalRepository.findById(approvalId)
                .orElseThrow(() -> new InventoryNotFoundException("Pending adjustment not found: " + approvalId));

        StockBalance balance = balanceRepository.findById(approval.getStockBalanceId())
                .orElseThrow(() -> new InventoryNotFoundException("Stock balance not found: " + approval.getStockBalanceId()));

        BigDecimal result = balance.getQuantity().add(approval.getQuantityAdjustment());
        if (result.compareTo(BigDecimal.ZERO) < 0)
            throw new InvalidStockAdjustmentException("Adjustment cannot result in negative stock balance");

        balanceRepository.save(StockBalance.builder()
                .id(balance.getId())
                .warehouseId(balance.getWarehouseId()).locationId(balance.getLocationId())
                .productId(balance.getProductId()).lotId(balance.getLotId())
                .stockStatusId(balance.getStockStatusId()).quantity(result)
                .version(balance.getVersion() == null ? 1L : balance.getVersion() + 1)
                .createdAt(balance.getCreatedAt()).updatedAt(Instant.now())
                .build());

        UUID adjustmentTypeId = movementTypeRepository.findIdByName(MovementTypeConstants.ADJUSTMENT)
                .orElseThrow(() -> new InventoryNotFoundException("Movement type ADJUSTMENT not found"));

        UUID currentUserId = currentUserPort.getCurrentUserId();
        boolean positive = approval.getQuantityAdjustment().compareTo(BigDecimal.ZERO) > 0;
        StockMovement saved = movementRepository.save(StockMovement.create(
                adjustmentTypeId, balance.getProductId(), balance.getLotId(),
                positive ? null : balance.getWarehouseId(),
                positive ? null : balance.getLocationId(),
                positive ? balance.getWarehouseId() : null,
                positive ? balance.getLocationId() : null,
                approval.getQuantityAdjustment().abs(),
                balance.getStockStatusId(), balance.getStockStatusId(),
                approval.getReferenceNo(), approval.getReason(), currentUserId));

        approvalRepository.deleteById(approvalId);

        return movementRepository.findById(saved.getId())
                .map(m -> mapper.toDto(m))
                .orElse(mapper.toDto(saved));
    }

    @Override
    @Transactional
    public void rejectAdjustment(UUID approvalId) {
        approvalRepository.findById(approvalId)
                .orElseThrow(() -> new InventoryNotFoundException("Pending adjustment not found: " + approvalId));
        approvalRepository.deleteById(approvalId);
    }

    @Override
    @Transactional
    public StockTransferResponse transferStock(StockTransferRequest request) {
        if (request == null) throw new IllegalArgumentException("Stock transfer request cannot be null");
        if (request.getQuantity() == null || request.getQuantity().compareTo(BigDecimal.ZERO) <= 0)
            throw new IllegalArgumentException("Quantity must be greater than zero");
        if (request.getFromLocationId() != null && request.getFromLocationId().equals(request.getToLocationId()))
            throw new IllegalArgumentException("Source and destination location cannot be the same");

        if (!warehouseLocationQueryPort.belongsToWarehouse(request.getFromLocationId(), request.getFromWarehouseId()))
            throw new IllegalArgumentException("Source location does not belong to the specified source warehouse");
        if (!warehouseLocationQueryPort.belongsToWarehouse(request.getToLocationId(), request.getToWarehouseId()))
            throw new IllegalArgumentException("Destination location does not belong to the specified destination warehouse");

        UUID availableStatusId = stockStatusRepository.findIdByName(StockStatusConstants.AVAILABLE)
                .orElseThrow(() -> new InventoryNotFoundException("Stock status AVAILABLE not found"));
        UUID transferOutTypeId = movementTypeRepository.findIdByName(MovementTypeConstants.TRANSFER_OUT)
                .orElseThrow(() -> new InventoryNotFoundException("Movement type TRANSFER_OUT not found"));
        UUID transferInTypeId = movementTypeRepository.findIdByName(MovementTypeConstants.TRANSFER_IN)
                .orElseThrow(() -> new InventoryNotFoundException("Movement type TRANSFER_IN not found"));

        StockBalance source = balanceRepository.findForUpdate(
                request.getFromWarehouseId(), request.getFromLocationId(),
                request.getProductId(), request.getLotId(), availableStatusId)
                .orElseThrow(() -> new InsufficientStockException("Insufficient stock at source location"));
        source.deductQuantity(request.getQuantity());
        StockBalance updatedSource = balanceRepository.save(source);

        StockBalance dest = balanceRepository.findForUpdate(
                request.getToWarehouseId(), request.getToLocationId(),
                request.getProductId(), request.getLotId(), availableStatusId)
                .orElse(StockBalance.builder()
                        .id(UuidV7.generate())
                        .warehouseId(request.getToWarehouseId()).locationId(request.getToLocationId())
                        .productId(request.getProductId()).lotId(request.getLotId())
                        .stockStatusId(availableStatusId).quantity(BigDecimal.ZERO)
                        .version(1L).createdAt(Instant.now()).updatedAt(Instant.now())
                        .build());
        dest.addQuantity(request.getQuantity());
        StockBalance updatedDest = balanceRepository.save(dest);

        UUID currentUserId = currentUserPort.getCurrentUserId();

        StockMovement outMovement = movementRepository.save(StockMovement.create(
                transferOutTypeId, request.getProductId(), request.getLotId(),
                request.getFromWarehouseId(), request.getFromLocationId(),
                request.getToWarehouseId(), request.getToLocationId(),
                request.getQuantity(), availableStatusId, availableStatusId,
                null, "Stock Transfer Out", currentUserId));

        StockMovement inMovement = movementRepository.save(StockMovement.create(
                transferInTypeId, request.getProductId(), request.getLotId(),
                request.getFromWarehouseId(), request.getFromLocationId(),
                request.getToWarehouseId(), request.getToLocationId(),
                request.getQuantity(), availableStatusId, availableStatusId,
                null, "Stock Transfer In", currentUserId));

        return StockTransferResponse.builder()
                .transferOutMovement(mapper.toDto(outMovement))
                .transferInMovement(mapper.toDto(inMovement))
                .sourceBalance(mapper.toDto(updatedSource))
                .destinationBalance(mapper.toDto(updatedDest))
                .build();
    }
}
