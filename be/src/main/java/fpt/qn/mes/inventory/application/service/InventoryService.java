package fpt.qn.mes.inventory.application.service;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import fpt.qn.mes.audit.domain.entities.AuditAction;
import fpt.qn.mes.audit.domain.events.AuditEvent;
import fpt.qn.mes.auth.application.port.out.CurrentUserPort;
import fpt.qn.mes.common.dto.response.PageResponse;
import fpt.qn.mes.common.util.UuidV7;
import fpt.qn.mes.inventory.application.dto.product.ProductResponse;
import fpt.qn.mes.inventory.application.dto.response.StockTransferResponse;
import fpt.qn.mes.inventory.application.dto.stockadjustment.create.CreateStockAdjustmentRequest;
import fpt.qn.mes.inventory.application.dto.stockadjustmentapproval.StockAdjustmentApprovalResponse;
import fpt.qn.mes.inventory.application.dto.stockbalance.StockBalanceResponse;
import fpt.qn.mes.inventory.application.dto.stockbalance.search.StockBalanceSearchRequest;
import fpt.qn.mes.inventory.application.dto.stocklot.StockLotResponse;
import fpt.qn.mes.inventory.application.dto.stocklot.create.CreateStockLotRequest;
import fpt.qn.mes.inventory.application.dto.stocklot.search.StockLotSearchRequest;
import fpt.qn.mes.inventory.application.dto.stockmovement.StockMovementResponse;
import fpt.qn.mes.inventory.application.dto.stockmovement.create.CreateStockMovementRequest;
import fpt.qn.mes.inventory.application.dto.stockmovement.create.StockInRequest;
import fpt.qn.mes.inventory.application.dto.stockmovement.create.StockTransferRequest;
import fpt.qn.mes.inventory.application.dto.stockmovement.search.StockMovementSearchRequest;
import fpt.qn.mes.inventory.application.dto.user.UserResponse;
import fpt.qn.mes.inventory.application.dto.warehouse.WarehouseLocationResponse;
import fpt.qn.mes.inventory.application.dto.warehouse.WarehouseResponse;
import fpt.qn.mes.inventory.application.exception.InsufficientStockException;
import fpt.qn.mes.inventory.application.exception.InvalidStockAdjustmentException;
import fpt.qn.mes.inventory.application.exception.InvalidStockLotException;
import fpt.qn.mes.inventory.application.exception.InventoryNotFoundException;
import fpt.qn.mes.inventory.application.exception.StockLotConflictException;
import fpt.qn.mes.inventory.application.exception.StockLotNotFoundException;
import fpt.qn.mes.inventory.application.mapper.InventoryDtoMapper;
import fpt.qn.mes.inventory.application.mapper.StockAdjustmentApprovalDtoMapper;
import fpt.qn.mes.inventory.application.port.in.InventoryUseCase;
import fpt.qn.mes.inventory.domain.constants.MovementTypeConstants;
import fpt.qn.mes.inventory.domain.constants.StockStatusConstants;
import fpt.qn.mes.inventory.domain.entities.StockAdjustmentApproval;
import fpt.qn.mes.inventory.domain.entities.StockBalance;
import fpt.qn.mes.inventory.domain.entities.StockLot;
import fpt.qn.mes.inventory.domain.entities.StockMovement;
import fpt.qn.mes.inventory.domain.repository.LotTypeRepository;
import fpt.qn.mes.inventory.domain.repository.MovementTypeRepository;
import fpt.qn.mes.inventory.domain.repository.StockAdjustmentApprovalRepository;
import fpt.qn.mes.inventory.domain.repository.StockBalanceRepository;
import fpt.qn.mes.inventory.domain.repository.StockLotRepository;
import fpt.qn.mes.inventory.domain.repository.StockMovementRepository;
import fpt.qn.mes.inventory.domain.repository.StockStatusRepository;
import fpt.qn.mes.inventory.domain.repository.criteria.StockAdjustmentApprovalSearchCriteria;
import fpt.qn.mes.inventory.domain.repository.criteria.StockBalanceSearchCriteria;
import fpt.qn.mes.inventory.domain.repository.criteria.StockLotSearchCriteria;
import fpt.qn.mes.inventory.domain.repository.criteria.StockMovementSearchCriteria;
import fpt.qn.mes.master.location.application.port.in.WarehouseLocationUseCase;
import fpt.qn.mes.master.product.application.port.in.ProductUseCase;
import fpt.qn.mes.master.warehouse.application.port.in.WarehouseUseCase;
import fpt.qn.mes.user.domain.entities.User;
import fpt.qn.mes.user.domain.repository.UserRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;


@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class InventoryService implements InventoryUseCase {

    StockLotRepository lotRepository;
    LotTypeRepository lotTypeRepository;
    StockMovementRepository movementRepository;
    StockBalanceRepository balanceRepository;
    MovementTypeRepository movementTypeRepository;
    StockStatusRepository stockStatusRepository;
    UserRepository userRepository;
    StockAdjustmentApprovalRepository approvalRepository;
    InventoryDtoMapper mapper;
    StockAdjustmentApprovalDtoMapper approvalMapper;
    WarehouseUseCase warehouseUseCase;
    ProductUseCase productUseCase;
    WarehouseLocationUseCase warehouseLocationUseCase;
    CurrentUserPort currentUserPort;
    ApplicationEventPublisher eventPublisher;

    private static final BigDecimal ADJUSTMENT_THRESHOLD = new BigDecimal("100.00");

    @Override
    @Transactional(readOnly = true)
    public PageResponse<StockLotResponse> getStockLots(StockLotSearchRequest request) {
        StockLotSearchCriteria criteria = StockLotSearchCriteria.builder()
                .productId(request != null ? request.getProductId() : null)
                .lotTypeId(request != null ? request.getLotTypeId() : null)
                .lotNumber(request != null ? request.getLotNumber() : null)
                .expiryBefore(request != null ? request.getExpiryBefore() : null)
                .page(request != null ? request.getPage() : 0)
                .size(request != null ? request.getSize() : 20)
                .sort(request != null && request.getSort() != null ? request.getSort() : List.of())
                .build();
        long totalElements = lotRepository.count(criteria);
        List<StockLot> items = lotRepository.search(criteria);
        List<StockLotResponse> dtos = items.stream().map(mapper::toDto).toList();
        return PageResponse.of(dtos, totalElements, request != null ? request.getPage() : 0, request != null ? request.getSize() : 20);
    }

    @Override
    @Transactional(readOnly = true)
    public StockLotResponse getStockLotById(UUID id) {
        StockLot lot = lotRepository.findById(id)
                .orElseThrow(() -> new StockLotNotFoundException("Stock lot not found with ID: " + id));
        return mapper.toDto(lot);
    }

    @Override
    @Transactional
    public void createStockLot(CreateStockLotRequest request) {
        Optional<StockLot> existingLot = lotRepository.findByLotNumber(request.getLotNumber());
        if (existingLot.isPresent()) {
            StockLot lot = existingLot.get();
            if (!request.getProductId().equals(lot.getProductId())) {
                throw new InvalidStockLotException(
                        "Stock lot '" + request.getLotNumber() + "' belongs to a different product");
            }
            throw new StockLotConflictException(
                    "Stock lot '" + request.getLotNumber() + "' already exists");
        }

        StockLot lot = StockLot.create(
                request.getLotNumber(),
                request.getProductId(),
                request.getLotTypeId(),
                request.getExpiryDate()
        );
        lotRepository.save(lot);
    }

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

        long totalElements = movementRepository.count(criteria);
        List<StockMovement> items = movementRepository.search(criteria);

        if (items.isEmpty()) {
            return PageResponse.of(List.of(), totalElements, request != null ? request.getPage() : 0, request != null ? request.getSize() : 20);
        }

        Map<UUID, ProductResponse> productMap = buildProductMap(items);
        Map<UUID, WarehouseResponse> warehouseMap = buildWarehouseMap(items);
        Map<UUID, WarehouseLocationResponse> locationMap = buildLocationMap(items);
        Map<UUID, UserResponse> userMap = buildUserMap(items);

        List<StockMovementResponse> dtos = items.stream()
                .map(m -> populateMovementSummaryFields(m, productMap, warehouseMap, locationMap, userMap))
                .toList();

        return PageResponse.of(dtos, totalElements, request != null ? request.getPage() : 0, request != null ? request.getSize() : 20);
    }

    @Override
    @Transactional
    public void recordMovement(CreateStockMovementRequest request, UUID currentUserId) {
        if (request.getQuantity() == null || request.getQuantity().compareTo(BigDecimal.ZERO) <= 0) {
            throw new InsufficientStockException("Movement quantity must be positive");
        }

        // Validate FK references — each getXxxById() throws NotFoundException if absent
        warehouseUseCase.getWarehouseById(request.getWarehouseId());
        productUseCase.getProductById(request.getProductId());
        if (request.getLocationId() != null) {
            warehouseLocationUseCase.getWarehouseLocationById(request.getLocationId());
        }

        // 1. Handle deduction from source status balance if fromStatusId is present
        if (request.getFromStatusId() != null) {
            StockBalance fromBalance = balanceRepository.findForUpdate(
                    request.getWarehouseId(),
                    request.getLocationId(),
                    request.getProductId(),
                    request.getLotId(),
                    request.getFromStatusId()
            ).orElseThrow(() -> new InsufficientStockException("Insufficient stock balance for this movement"));

            fromBalance.deductQuantity(request.getQuantity());
            balanceRepository.save(fromBalance);
        }

        // 2. Handle addition to destination status balance if toStatusId is present
        if (request.getToStatusId() != null) {
            Optional<StockBalance> optToBalance = balanceRepository.findForUpdate(
                    request.getWarehouseId(),
                    request.getLocationId(),
                    request.getProductId(),
                    request.getLotId(),
                    request.getToStatusId()
            );

            StockBalance toBalance;
            if (optToBalance.isPresent()) {
                toBalance = optToBalance.get();
                toBalance.addQuantity(request.getQuantity());
            } else {
                toBalance = StockBalance.builder()
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
                        .build();
            }
            balanceRepository.save(toBalance);
        }

        UUID fromWh = request.getFromStatusId() != null ? request.getWarehouseId() : null;
        UUID fromLoc = request.getFromStatusId() != null ? request.getLocationId() : null;
        UUID toWh = (request.getToStatusId() != null || request.getFromStatusId() == null) ? request.getWarehouseId() : null;
        UUID toLoc = (request.getToStatusId() != null || request.getFromStatusId() == null) ? request.getLocationId() : null;


        StockMovement movement = StockMovement.create(
                request.getMovementTypeId(),
                request.getProductId(),
                request.getLotId(),
                fromWh,
                fromLoc,
                toWh,
                toLoc,
                request.getQuantity(),
                request.getFromStatusId(),
                request.getToStatusId(),
                request.getReferenceNo(),
                request.getReason(),
                currentUserId
        );

        movementRepository.save(movement);
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
        long totalElements = balanceRepository.count(criteria);
        List<StockBalance> balances = balanceRepository.search(criteria);
        List<StockBalanceResponse> dtos = balances.stream().map(mapper::toDto).toList();
        return PageResponse.of(dtos, totalElements, request != null ? request.getPage() : 0, request != null ? request.getSize() : 20);
    }

    @Override
    @Transactional
    public void recordStockIn(StockInRequest request, UUID currentUserId) {

        // Validate master data references
        productUseCase.getProductById(request.getProductId());
        warehouseUseCase.getWarehouseById(request.getWarehouseId());
        warehouseLocationUseCase.getWarehouseLocationById(request.getLocationId());

        // Resolve or create StockLot with lotNumber uniqueness / product ownership validation
        StockLot lot;
        Optional<StockLot> optExistingLot = lotRepository.findByLotNumber(request.getLotNumber());
        if (optExistingLot.isPresent()) {
            lot = optExistingLot.get();
            if (!request.getProductId().equals(lot.getProductId())) {
                throw new InvalidStockLotException(
                        "Stock lot '" + request.getLotNumber() + "' belongs to a different product");
            }
        } else {
            StockLot newLot = StockLot.create(
                    request.getLotNumber(),
                    request.getProductId(),
                    request.getLotTypeId(),
                    request.getExpiryDate()
            );
            lot = lotRepository.save(newLot);
        }

        // Determine stock status (defaulting to AVAILABLE)
        UUID statusId = stockStatusRepository.findIdByName(StockStatusConstants.AVAILABLE)
                .orElseThrow(() -> new InventoryNotFoundException("Stock status AVAILABLE not found"));

        // Lookup movement type ID for PURCHASE_IN
        UUID purchaseInTypeId = movementTypeRepository.findIdByName(MovementTypeConstants.PURCHASE_IN)
                .orElseThrow(() -> new InventoryNotFoundException("Movement type PURCHASE_IN not found"));

        // Find or create StockBalance for UPDATE
        Optional<StockBalance> optBalance = balanceRepository.findForUpdate(
                request.getWarehouseId(),
                request.getLocationId(),
                request.getProductId(),
                lot.getId(),
                statusId
        );

        BigDecimal currentOnHand = optBalance.map(StockBalance::getQuantity).orElse(BigDecimal.ZERO);
        BigDecimal newQuantity = currentOnHand.add(request.getQuantity());

        StockBalance balanceToSave = StockBalance.builder()
                .id(optBalance.map(b -> b.getId()).orElse(UuidV7.generate()))
                .warehouseId(request.getWarehouseId())
                .locationId(request.getLocationId())
                .productId(request.getProductId())
                .lotId(lot.getId())
                .stockStatusId(statusId)
                .quantity(newQuantity)
                .version(optBalance.map(b -> b.getVersion() == null ? 1L : b.getVersion() + 1).orElse(1L))
                .createdAt(optBalance.map(StockBalance::getCreatedAt).orElse(Instant.now()))
                .updatedAt(Instant.now())
                .build();

        balanceRepository.save(balanceToSave);

        // Record stock movement ledger entry
        StockMovement movement = StockMovement.create(
                purchaseInTypeId,
                request.getProductId(),
                lot.getId(),
                null,
                null,
                request.getWarehouseId(),
                request.getLocationId(),
                request.getQuantity(),
                null,
                statusId,
                request.getReferenceNo(),
                request.getReason(),
                currentUserId
        );

        movementRepository.save(movement);
    }

    private Map<UUID, ProductResponse> buildProductMap(List<StockMovement> items) {
        Set<UUID> ids = items.stream().map(StockMovement::getProductId).filter(Objects::nonNull).collect(Collectors.toSet());
        if (ids.isEmpty()) return Map.of();
        java.util.Map<UUID, ProductResponse> map = new java.util.HashMap<>();
        for (UUID id : ids) {
            try {
                fpt.qn.mes.master.product.application.dto.product.ProductResponse m = productUseCase.getProductById(id);
                map.put(id, ProductResponse.builder().id(m.getId()).code(m.getCode()).name(m.getName()).build());
            } catch (Exception e) {
                log.warn("Product not found: " + id);
            }
        }
        return map;
    }

    private Map<UUID, WarehouseResponse> buildWarehouseMap(List<StockMovement> items) {
        Set<UUID> ids = items.stream()
                .flatMap(m -> Stream.<UUID>of(m.getFromWarehouseId(), m.getToWarehouseId(), m.getWarehouseId()))
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        if (ids.isEmpty()) return Map.of();
        java.util.Map<UUID, WarehouseResponse> map = new java.util.HashMap<>();
        for (UUID id : ids) {
            try {
                fpt.qn.mes.master.warehouse.application.dto.warehouse.WarehouseResponse m = warehouseUseCase.getWarehouseById(id);
                map.put(id, WarehouseResponse.builder().id(m.getId()).code(m.getCode()).name(m.getName()).build());
            } catch (Exception e) {
                log.warn("Warehouse not found: " + id);
            }
        }
        return map;
    }

    private Map<UUID, WarehouseLocationResponse> buildLocationMap(List<StockMovement> items) {
        Set<UUID> ids = items.stream()
                .flatMap(m -> Stream.<UUID>of(m.getFromLocationId(), m.getToLocationId(), m.getLocationId()))
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        if (ids.isEmpty()) return Map.of();
        java.util.Map<UUID, WarehouseLocationResponse> map = new java.util.HashMap<>();
        for (UUID id : ids) {
            try {
                fpt.qn.mes.master.location.application.dto.warehouselocation.WarehouseLocationResponse m = warehouseLocationUseCase.getWarehouseLocationById(id);
                map.put(id, WarehouseLocationResponse.builder().id(m.getId()).code(m.getCode()).name(m.getName()).build());
            } catch (Exception e) {
                log.warn("Location not found: " + id);
            }
        }
        return map;
    }

    private Map<UUID, UserResponse> buildUserMap(List<StockMovement> items) {
        Set<UUID> ids = items.stream().map(StockMovement::getCreatedBy).filter(Objects::nonNull).collect(Collectors.toSet());
        if (ids.isEmpty()) return Map.of();
        return ids.stream()
                .map(userRepository::findById)
                .filter(Objects::nonNull)
                .flatMap(Optional::stream)
                .collect(Collectors.toMap(User::getId, user -> UserResponse.builder().id(user.getId()).username(user.getUsername()).fullName(user.getFullName()).build(), (u1, u2) -> u1));
    }

    private StockMovementResponse populateMovementSummaryFields(
            StockMovement m,
            Map<UUID, ProductResponse> productMap,
            Map<UUID, WarehouseResponse> warehouseMap,
            Map<UUID, WarehouseLocationResponse> locationMap,
            Map<UUID, UserResponse> userMap) {
        StockMovementResponse dto = mapper.toDto(m);
        if (dto != null && m != null) {
            if (m.getProductId() != null) {
                dto.setProduct(productMap.get(m.getProductId()));
            }
            if (m.getFromWarehouseId() != null) {
                dto.setFromWarehouse(warehouseMap.get(m.getFromWarehouseId()));
            }
            if (m.getToWarehouseId() != null) {
                dto.setToWarehouse(warehouseMap.get(m.getToWarehouseId()));
            }
            if (m.getFromLocationId() != null) {
                dto.setFromLocation(locationMap.get(m.getFromLocationId()));
            }
            if (m.getToLocationId() != null) {
                dto.setToLocation(locationMap.get(m.getToLocationId()));
            }
            if (m.getCreatedBy() != null) {
                dto.setCreatedBy(userMap.get(m.getCreatedBy()));
            }
        }
        return dto;
    }

    private StockMovementResponse enrichMovementDto(StockMovement m) {
        if (m == null) return null;
        List<StockMovement> items = List.of(m);
        Map<UUID, ProductResponse> productMap = buildProductMap(items);
        Map<UUID, WarehouseResponse> warehouseMap = buildWarehouseMap(items);
        Map<UUID, WarehouseLocationResponse> locationMap = buildLocationMap(items);
        Map<UUID, UserResponse> userMap = buildUserMap(items);
        return populateMovementSummaryFields(m, productMap, warehouseMap, locationMap, userMap);
    }

    @Override
    @Transactional
    public void adjustStock(CreateStockAdjustmentRequest request, UUID currentUserId) {
        if (request == null) {
            throw new InvalidStockAdjustmentException("Request body is required");
        }
        if (request.getReason() == null || request.getReason().isBlank()) {
            throw new InvalidStockAdjustmentException("Reason is required for stock adjustment");
        }
        if (request.getStockBalanceId() == null) {
            throw new InvalidStockAdjustmentException("Stock balance ID is required");
        }
        if (request.getQuantityAdjustment() == null) {
            throw new InvalidStockAdjustmentException("Quantity adjustment is required");
        }

        StockBalance balance = balanceRepository.findById(request.getStockBalanceId())
                .orElseThrow(() -> new InventoryNotFoundException("Stock balance not found with ID: " + request.getStockBalanceId()));

        BigDecimal resultingQuantity = balance.getQuantity().add(request.getQuantityAdjustment());
        if (resultingQuantity.compareTo(BigDecimal.ZERO) < 0) {
            throw new InvalidStockAdjustmentException("Adjustment cannot result in negative stock balance");
        }

        BigDecimal adjustmentAbs = request.getQuantityAdjustment().abs();
        boolean requiresApproval = adjustmentAbs.compareTo(ADJUSTMENT_THRESHOLD) > 0;

        if (requiresApproval) {
            StockAdjustmentApproval approval = StockAdjustmentApproval.create(
                    balance.getProductId(),
                    balance.getWarehouseId(),
                    balance.getLocationId(),
                    balance.getId(),
                    request.getQuantityAdjustment(),
                    request.getReason(),
                    request.getReferenceNo(),
                    currentUserId
            );
            StockAdjustmentApproval savedApproval = approvalRepository.save(approval);
            return;
        }

        StockBalance updatedBalance = StockBalance.builder()
                .id(balance.getId())
                .warehouseId(balance.getWarehouseId())
                .locationId(balance.getLocationId())
                .productId(balance.getProductId())
                .lotId(balance.getLotId())
                .stockStatusId(balance.getStockStatusId())
                .quantity(resultingQuantity)
                .version(balance.getVersion() == null ? 1L : balance.getVersion() + 1)
                .createdAt(balance.getCreatedAt())
                .updatedAt(Instant.now())
                .build();
        balanceRepository.save(updatedBalance);

        UUID adjustmentTypeId = movementTypeRepository.findIdByName(MovementTypeConstants.ADJUSTMENT)
                .orElseThrow(() -> new InventoryNotFoundException("Movement type ADJUSTMENT not found"));

        StockMovement movement;
        if (request.getQuantityAdjustment().compareTo(BigDecimal.ZERO) > 0) {
            movement = StockMovement.create(
                    adjustmentTypeId,
                    balance.getProductId(),
                    balance.getLotId(),
                    null,
                    null,
                    balance.getWarehouseId(),
                    balance.getLocationId(),
                    adjustmentAbs,
                    balance.getStockStatusId(),
                    balance.getStockStatusId(),
                    request.getReferenceNo(),
                    request.getReason(),
                    currentUserId
            );
        } else {
            movement = StockMovement.create(
                    adjustmentTypeId,
                    balance.getProductId(),
                    balance.getLotId(),
                    balance.getWarehouseId(),
                    balance.getLocationId(),
                    null,
                    null,
                    adjustmentAbs,
                    balance.getStockStatusId(),
                    balance.getStockStatusId(),
                    request.getReferenceNo(),
                    request.getReason(),
                    currentUserId
            );
        }

         
        StockMovement savedMovement = movementRepository.save(movement);
        StockMovementResponse movementDto = enrichMovementDto(savedMovement);

        if (eventPublisher != null) {
            eventPublisher.publishEvent(AuditEvent.create(currentUserId, AuditAction.ADJUST_STOCK, "STOCK_BALANCE", balance.getId(),
                    "{\"quantity\":" + balance.getQuantity() + "}",
                    "{\"quantity\":" + resultingQuantity + "}", null));
        }

        return;
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<StockAdjustmentApprovalResponse> getPendingAdjustments(StockAdjustmentApprovalSearchCriteria criteria) {
        if (criteria == null) {
            criteria = StockAdjustmentApprovalSearchCriteria.builder().page(0).size(20).build();
        }
        long totalElements = approvalRepository.count(criteria);
        List<StockAdjustmentApproval> pendingList = approvalRepository.search(criteria);

        Map<UUID, ProductResponse> productMap = buildApprovalProductMap(pendingList);
        Map<UUID, WarehouseResponse> warehouseMap = buildApprovalWarehouseMap(pendingList);
        Map<UUID, WarehouseLocationResponse> locationMap = buildApprovalLocationMap(pendingList);
        Map<UUID, UserResponse> userMap = buildApprovalUserMap(pendingList);

        List<StockAdjustmentApprovalResponse> dtos = pendingList.stream()
                .map(approval -> populateApprovalSummaryFields(approval, productMap, warehouseMap, locationMap, userMap))
                .toList();

        return PageResponse.of(dtos, totalElements, criteria.getPage(), criteria.getSize());
    }

    private Map<UUID, ProductResponse> buildApprovalProductMap(List<StockAdjustmentApproval> items) {
        Set<UUID> ids = items.stream().map(StockAdjustmentApproval::getProductId).filter(Objects::nonNull).collect(Collectors.toSet());
        if (ids.isEmpty()) return Map.of();
        java.util.Map<UUID, ProductResponse> map = new java.util.HashMap<>();
        for (UUID id : ids) {
            try {
                fpt.qn.mes.master.product.application.dto.product.ProductResponse m = productUseCase.getProductById(id);
                map.put(id, ProductResponse.builder().id(m.getId()).code(m.getCode()).name(m.getName()).build());
            } catch (Exception e) {
                log.warn("Product not found: " + id);
            }
        }
        return map;
    }

    private Map<UUID, WarehouseResponse> buildApprovalWarehouseMap(List<StockAdjustmentApproval> items) {
        Set<UUID> ids = items.stream().map(StockAdjustmentApproval::getWarehouseId).filter(Objects::nonNull).collect(Collectors.toSet());
        if (ids.isEmpty()) return Map.of();
        java.util.Map<UUID, WarehouseResponse> map = new java.util.HashMap<>();
        for (UUID id : ids) {
            try {
                fpt.qn.mes.master.warehouse.application.dto.warehouse.WarehouseResponse m = warehouseUseCase.getWarehouseById(id);
                map.put(id, WarehouseResponse.builder().id(m.getId()).code(m.getCode()).name(m.getName()).build());
            } catch (Exception e) {
                log.warn("Warehouse not found: " + id);
            }
        }
        return map;
    }

    private Map<UUID, WarehouseLocationResponse> buildApprovalLocationMap(List<StockAdjustmentApproval> items) {
        Set<UUID> ids = items.stream().map(StockAdjustmentApproval::getLocationId).filter(Objects::nonNull).collect(Collectors.toSet());
        if (ids.isEmpty()) return Map.of();
        java.util.Map<UUID, WarehouseLocationResponse> map = new java.util.HashMap<>();
        for (UUID id : ids) {
            try {
                fpt.qn.mes.master.location.application.dto.warehouselocation.WarehouseLocationResponse m = warehouseLocationUseCase.getWarehouseLocationById(id);
                map.put(id, WarehouseLocationResponse.builder().id(m.getId()).code(m.getCode()).name(m.getName()).build());
            } catch (Exception e) {
                log.warn("Location not found: " + id);
            }
        }
        return map;
    }

    private Map<UUID, UserResponse> buildApprovalUserMap(List<StockAdjustmentApproval> items) {
        Set<UUID> ids = items.stream().map(StockAdjustmentApproval::getCreatedBy).filter(Objects::nonNull).collect(Collectors.toSet());
        if (ids.isEmpty()) return Map.of();
        return ids.stream()
                .map(userRepository::findById)
                .filter(Objects::nonNull)
                .flatMap(Optional::stream)
                .collect(Collectors.toMap(User::getId, user -> UserResponse.builder().id(user.getId()).username(user.getUsername()).fullName(user.getFullName()).build(), (u1, u2) -> u1));
    }

    private StockAdjustmentApprovalResponse populateApprovalSummaryFields(
            StockAdjustmentApproval approval,
            Map<UUID, ProductResponse> productMap,
            Map<UUID, WarehouseResponse> warehouseMap,
            Map<UUID, WarehouseLocationResponse> locationMap,
            Map<UUID, UserResponse> userMap) {
        if (approval == null) {
            return null;
        }
        StockAdjustmentApprovalResponse dto = approvalMapper.toDto(approval);
        if (dto != null) {
            if (approval.getProductId() != null && productMap != null) {
                dto.setProduct(productMap.get(approval.getProductId()));
            }
            if (approval.getWarehouseId() != null && warehouseMap != null) {
                dto.setWarehouse(warehouseMap.get(approval.getWarehouseId()));
            }
            if (approval.getLocationId() != null && locationMap != null) {
                dto.setLocation(locationMap.get(approval.getLocationId()));
            }
            if (approval.getCreatedBy() != null && userMap != null) {
                dto.setCreator(userMap.get(approval.getCreatedBy()));
            }
        }
        return dto;
    }

    @Override
    @Transactional
    public StockMovementResponse approveAdjustment(UUID approvalId, UUID currentUserId) {
        StockAdjustmentApproval approval = approvalRepository.findById(approvalId)
                .orElseThrow(() -> new InventoryNotFoundException("Pending adjustment request not found with ID: " + approvalId));

        StockBalance balance = balanceRepository.findById(approval.getStockBalanceId())
                .orElseThrow(() -> new InventoryNotFoundException("Stock balance not found with ID: " + approval.getStockBalanceId()));

        BigDecimal resultingQuantity = balance.getQuantity().add(approval.getQuantityAdjustment());
        if (resultingQuantity.compareTo(BigDecimal.ZERO) < 0) {
            throw new InvalidStockAdjustmentException("Adjustment cannot result in negative stock balance");
        }

        StockBalance updatedBalance = StockBalance.builder()
                .id(balance.getId())
                .warehouseId(balance.getWarehouseId())
                .locationId(balance.getLocationId())
                .productId(balance.getProductId())
                .lotId(balance.getLotId())
                .stockStatusId(balance.getStockStatusId())
                .quantity(resultingQuantity)
                .version(balance.getVersion() == null ? 1L : balance.getVersion() + 1)
                .createdAt(balance.getCreatedAt())
                .updatedAt(Instant.now())
                .build();
        balanceRepository.save(updatedBalance);

        UUID adjustmentTypeId = movementTypeRepository.findIdByName(MovementTypeConstants.ADJUSTMENT)
                .orElseThrow(() -> new InventoryNotFoundException("Movement type ADJUSTMENT not found"));

        UUID fromWh = approval.getQuantityAdjustment().compareTo(BigDecimal.ZERO) < 0 ? balance.getWarehouseId() : null;
        UUID fromLoc = approval.getQuantityAdjustment().compareTo(BigDecimal.ZERO) < 0 ? balance.getLocationId() : null;
        UUID toWh = approval.getQuantityAdjustment().compareTo(BigDecimal.ZERO) > 0 ? balance.getWarehouseId() : null;
        UUID toLoc = approval.getQuantityAdjustment().compareTo(BigDecimal.ZERO) > 0 ? balance.getLocationId() : null;

        StockMovement movement = StockMovement.create(
                adjustmentTypeId,
                balance.getProductId(),
                balance.getLotId(),
                fromWh,
                fromLoc,
                toWh,
                toLoc,
                approval.getQuantityAdjustment().abs(),
                balance.getStockStatusId(),
                balance.getStockStatusId(),
                approval.getReferenceNo(),
                approval.getReason(),
                currentUserId
        );
        StockMovement savedMovement = movementRepository.save(movement);

        approvalRepository.deleteById(approvalId);

        return enrichMovementDto(savedMovement);
    }

    @Override
    @Transactional
    public void rejectAdjustment(UUID approvalId, UUID currentUserId) {
        StockAdjustmentApproval approval = approvalRepository.findById(approvalId)
                .orElseThrow(() -> new InventoryNotFoundException("Pending adjustment request not found with ID: " + approvalId));

        approvalRepository.deleteById(approvalId);
    }

    

    

    

    @Override
    @Transactional
    public StockTransferResponse transferStock(
            StockTransferRequest request, UUID currentUserId) {
        if (request == null) {
            throw new IllegalArgumentException("Stock transfer request cannot be null");
        }
        if (request.getQuantity() == null || request.getQuantity().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Quantity must be greater than zero");
        }
        if (request.getFromLocationId() != null && request.getFromLocationId().equals(request.getToLocationId())) {
            throw new IllegalArgumentException("Source and destination location cannot be the same");
        }

        var fromLoc = warehouseLocationUseCase.getWarehouseLocationById(request.getFromLocationId());
        var toLoc = warehouseLocationUseCase.getWarehouseLocationById(request.getToLocationId());

        if (!Objects.equals(fromLoc.getWarehouse().getId(), request.getFromWarehouseId())) {
            throw new IllegalArgumentException("Source location does not belong to the specified source warehouse");
        }

        if (!Objects.equals(toLoc.getWarehouse().getId(), request.getToWarehouseId())) {
            throw new IllegalArgumentException("Destination location does not belong to the specified destination warehouse");
        }

        UUID availableStatusId = stockStatusRepository.findIdByName(StockStatusConstants.AVAILABLE)
                .orElseThrow(() -> new InventoryNotFoundException("Stock status AVAILABLE not found"));

        UUID transferOutTypeId = movementTypeRepository.findIdByName(MovementTypeConstants.TRANSFER_OUT)
                .orElseThrow(() -> new InventoryNotFoundException("Movement type TRANSFER_OUT not found"));

        UUID transferInTypeId = movementTypeRepository.findIdByName(MovementTypeConstants.TRANSFER_IN)
                .orElseThrow(() -> new InventoryNotFoundException("Movement type TRANSFER_IN not found"));


        StockBalance sourceBalance = balanceRepository.findForUpdate(
                request.getFromWarehouseId(),
                request.getFromLocationId(),
                request.getProductId(),
                request.getLotId(),
                availableStatusId
        ).orElseThrow(() -> new InsufficientStockException("Insufficient stock at source location"));

        sourceBalance.deductQuantity(request.getQuantity());
        StockBalance updatedSourceBalance = balanceRepository.save(sourceBalance);

        StockBalance destBalance = balanceRepository.findForUpdate(
                request.getToWarehouseId(),
                request.getToLocationId(),
                request.getProductId(),
                request.getLotId(),
                availableStatusId
        ).orElse(StockBalance.builder()
                .id(UuidV7.generate())
                .warehouseId(request.getToWarehouseId())
                .locationId(request.getToLocationId())
                .productId(request.getProductId())
                .lotId(request.getLotId())
                .stockStatusId(availableStatusId)
                .quantity(BigDecimal.ZERO)
                .version(1L)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build());

        destBalance.addQuantity(request.getQuantity());
        StockBalance updatedDestBalance = balanceRepository.save(destBalance);

        StockMovement outMovement = StockMovement.create(
                transferOutTypeId,
                request.getProductId(),
                request.getLotId(),
                request.getFromWarehouseId(),
                request.getFromLocationId(),
                request.getToWarehouseId(),
                request.getToLocationId(),
                request.getQuantity(),
                availableStatusId,
                availableStatusId,
                null,
                "Stock Transfer Out",
                currentUserId
        );
        StockMovement savedOutMovement = movementRepository.save(outMovement);

        StockMovement inMovement = StockMovement.create(
                transferInTypeId,
                request.getProductId(),
                request.getLotId(),
                request.getFromWarehouseId(),
                request.getFromLocationId(),
                request.getToWarehouseId(),
                request.getToLocationId(),
                request.getQuantity(),
                availableStatusId,
                availableStatusId,
                null,
                "Stock Transfer In",
                currentUserId
        );
        StockMovement savedInMovement = movementRepository.save(inMovement);

        return StockTransferResponse.builder()
                .transferOutMovement(mapper.toDto(savedOutMovement))
                .transferInMovement(mapper.toDto(savedInMovement))
                .sourceBalance(mapper.toDto(updatedSourceBalance))
                .destinationBalance(mapper.toDto(updatedDestBalance))
                .build();
    }
}
