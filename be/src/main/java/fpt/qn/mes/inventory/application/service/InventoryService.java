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

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import fpt.qn.mes.common.dto.response.PageResponse;
import fpt.qn.mes.inventory.application.dto.request.CreateMovementRequest;
import fpt.qn.mes.inventory.application.dto.request.CreateStockLotRequest;
import fpt.qn.mes.inventory.application.dto.request.StockBalanceSearchRequest;
import fpt.qn.mes.inventory.application.dto.request.StockInRequest;
import fpt.qn.mes.inventory.application.dto.request.StockLotSearchRequest;
import fpt.qn.mes.inventory.application.dto.request.StockMovementSearchRequest;
import fpt.qn.mes.inventory.application.dto.response.LocationSummaryDto;
import fpt.qn.mes.inventory.application.dto.response.ProductSummaryDto;
import fpt.qn.mes.inventory.application.dto.response.StockBalanceDto;
import fpt.qn.mes.inventory.application.dto.response.StockLotDto;
import fpt.qn.mes.inventory.application.dto.response.StockMovementDto;
import fpt.qn.mes.inventory.application.dto.response.UserSummaryDto;
import fpt.qn.mes.inventory.application.dto.response.WarehouseSummaryDto;
import fpt.qn.mes.inventory.application.exception.InsufficientStockException;
import fpt.qn.mes.inventory.application.exception.InvalidStockLotException;
import fpt.qn.mes.inventory.application.exception.InventoryNotFoundException;
import fpt.qn.mes.inventory.application.exception.StockLotConflictException;
import fpt.qn.mes.inventory.application.exception.StockLotNotFoundException;
import fpt.qn.mes.inventory.application.mapper.InventoryDtoMapper;
import fpt.qn.mes.inventory.application.port.in.InventoryUseCase;
import fpt.qn.mes.inventory.domain.constants.MovementTypeConstants;
import fpt.qn.mes.inventory.domain.constants.StockStatusConstants;
import fpt.qn.mes.inventory.domain.entities.StockBalance;
import fpt.qn.mes.inventory.domain.entities.StockLot;
import fpt.qn.mes.inventory.domain.entities.StockMovement;
import fpt.qn.mes.inventory.domain.repository.MovementTypeRepository;
import fpt.qn.mes.inventory.domain.repository.StockBalanceRepository;
import fpt.qn.mes.inventory.domain.repository.StockLotRepository;
import fpt.qn.mes.inventory.domain.repository.StockMovementRepository;
import fpt.qn.mes.inventory.domain.repository.StockStatusRepository;
import fpt.qn.mes.inventory.domain.repository.criteria.StockBalanceSearchCriteria;
import fpt.qn.mes.inventory.domain.repository.criteria.StockLotSearchCriteria;
import fpt.qn.mes.inventory.domain.repository.criteria.StockMovementSearchCriteria;
import fpt.qn.mes.common.util.UuidV7;
import fpt.qn.mes.master.location.application.port.in.LocationUseCase;
import fpt.qn.mes.master.product.application.port.in.ProductUseCase;
import fpt.qn.mes.master.warehouse.application.port.in.WarehouseUseCase;
import fpt.qn.mes.user.domain.entities.User;
import fpt.qn.mes.user.domain.repository.UserRepository;

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
    UserRepository userRepository;
    InventoryDtoMapper mapper;
    WarehouseUseCase warehouseUseCase;
    ProductUseCase productUseCase;
    LocationUseCase locationUseCase;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<StockLotDto> getStockLots(StockLotSearchRequest request) {
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
        List<StockLotDto> dtos = items.stream().map(mapper::toDto).toList();
        return PageResponse.of(dtos, totalElements, request != null ? request.getPage() : 0, request != null ? request.getSize() : 20);
    }

    @Override
    @Transactional(readOnly = true)
    public StockLotDto getStockLotById(UUID id) {
        StockLot lot = lotRepository.findById(id)
                .orElseThrow(() -> new StockLotNotFoundException("Stock lot not found with ID: " + id));
        return mapper.toDto(lot);
    }

    @Override
    @Transactional
    public StockLotDto createStockLot(CreateStockLotRequest request) {
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
        StockLot saved = lotRepository.save(lot);
        return mapper.toDto(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<StockMovementDto> getMovements(StockMovementSearchRequest request) {
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

        Map<UUID, ProductSummaryDto> productMap = buildProductMap(items);
        Map<UUID, WarehouseSummaryDto> warehouseMap = buildWarehouseMap(items);
        Map<UUID, LocationSummaryDto> locationMap = buildLocationMap(items);
        Map<UUID, UserSummaryDto> userMap = buildUserMap(items);

        List<StockMovementDto> dtos = items.stream()
                .map(m -> {
                    StockMovementDto dto = mapper.toDto(m);
                    if (dto != null) {
                        if (m.getProductId() != null) {
                            dto.setProduct(productMap.get(m.getProductId()));
                        }
                        if (m.getWarehouseId() != null) {
                            dto.setToWarehouse(warehouseMap.get(m.getWarehouseId()));
                        }
                        if (m.getLocationId() != null) {
                            dto.setToLocation(locationMap.get(m.getLocationId()));
                        }
                        if (m.getCreatedBy() != null) {
                            dto.setCreatedBy(userMap.get(m.getCreatedBy()));
                        }
                    }
                    return dto;
                })
                .toList();

        return PageResponse.of(dtos, totalElements, request != null ? request.getPage() : 0, request != null ? request.getSize() : 20);
    }

    @Override
    @Transactional
    public StockMovementDto recordMovement(CreateMovementRequest request, UUID currentUserId) {
        if (request.getQuantity() == null || request.getQuantity().compareTo(BigDecimal.ZERO) <= 0) {
            throw new InsufficientStockException("Movement quantity must be positive");
        }

        // Validate FK references — each getXxxById() throws NotFoundException if absent
        warehouseUseCase.getWarehouseById(request.getWarehouseId());
        productUseCase.getProductById(request.getProductId());
        if (request.getLocationId() != null) {
            locationUseCase.getLocationById(request.getLocationId());
        }

        Optional<StockBalance> optBalance = balanceRepository.findForUpdate(
                request.getWarehouseId(),
                request.getLocationId(),
                request.getProductId(),
                request.getLotId()
        );

        BigDecimal currentOnHand = optBalance.map(StockBalance::getQuantity).orElse(BigDecimal.ZERO);
        BigDecimal newQuantity;

        if (request.getFromStatusId() != null && request.getToStatusId() == null) {
            if (currentOnHand.compareTo(request.getQuantity()) < 0) {
                throw new InsufficientStockException("Insufficient stock balance for this movement");
            }
            newQuantity = currentOnHand.subtract(request.getQuantity());
        } else {
            newQuantity = currentOnHand.add(request.getQuantity());
        }

        StockBalance balanceToSave = StockBalance.builder()
                .id(optBalance.map(b -> b.getId()).orElse(UuidV7.generate()))
                .warehouseId(request.getWarehouseId())
                .locationId(request.getLocationId())
                .productId(request.getProductId())
                .lotId(request.getLotId())
                .stockStatusId(request.getToStatusId() != null ? request.getToStatusId() : request.getFromStatusId())
                .quantity(newQuantity)
                .version(optBalance.map(b -> b.getVersion() == null ? 1L : b.getVersion() + 1).orElse(1L))
                .createdAt(optBalance.map(StockBalance::getCreatedAt).orElse(Instant.now()))
                .updatedAt(Instant.now())
                .build();

        balanceRepository.save(balanceToSave);

        StockMovement movement = StockMovement.create(
                request.getMovementTypeId(),
                request.getProductId(),
                request.getLotId(),
                request.getWarehouseId(),
                request.getLocationId(),
                request.getQuantity(),
                request.getFromStatusId(),
                request.getToStatusId(),
                request.getReferenceNo(),
                request.getReason(),
                currentUserId
        );

        StockMovement savedMovement = movementRepository.save(movement);
        return movementRepository.search(StockMovementSearchCriteria.builder().referenceNo(savedMovement.getReferenceNo()).build()).stream().findFirst().map(mapper::toDto).orElseGet(() -> mapper.toDto(savedMovement));
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<StockBalanceDto> getStockBalances(StockBalanceSearchRequest request) {
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
        List<StockBalanceDto> dtos = balances.stream().map(mapper::toDto).toList();
        return PageResponse.of(dtos, totalElements, request != null ? request.getPage() : 0, request != null ? request.getSize() : 20);
    }

    @Override
    @Transactional
    public StockMovementDto recordStockIn(StockInRequest request, UUID currentUserId) {

        // Validate master data references
        productUseCase.getProductById(request.getProductId());
        warehouseUseCase.getWarehouseById(request.getWarehouseId());
        locationUseCase.getLocationById(request.getLocationId());

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
                lot.getId()
        );

        BigDecimal currentOnHand = optBalance.map(StockBalance::getQuantity).orElse(BigDecimal.ZERO);
        BigDecimal newQuantity = currentOnHand.add(request.getQuantity());

        StockBalance balanceToSave = StockBalance.builder()
                .id(optBalance.map(StockBalance::getId).orElse(UuidV7.generate()))
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
                request.getWarehouseId(),
                request.getLocationId(),
                request.getQuantity(),
                null,
                statusId,
                request.getReferenceNo(),
                request.getReason(),
                currentUserId
        );

        StockMovement savedMovement = movementRepository.save(movement);
        return movementRepository.search(StockMovementSearchCriteria.builder().referenceNo(savedMovement.getReferenceNo()).build()).stream().findFirst().map(mapper::toDto).orElseGet(() -> mapper.toDto(savedMovement));
    }

    private Map<UUID, ProductSummaryDto> buildProductMap(List<StockMovement> items) {
        Set<UUID> ids = items.stream().map(StockMovement::getProductId).filter(Objects::nonNull).collect(Collectors.toSet());
        if (ids.isEmpty()) return Map.of();
        var res = productUseCase.getProductsByIds(ids);
        if (res == null) return Map.of();
        return res.entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, e -> ProductSummaryDto.builder().id(e.getValue().getId()).code(e.getValue().getCode()).name(e.getValue().getName()).build()));
    }

    private Map<UUID, WarehouseSummaryDto> buildWarehouseMap(List<StockMovement> items) {
        Set<UUID> ids = items.stream().map(StockMovement::getWarehouseId).filter(Objects::nonNull).collect(Collectors.toSet());
        if (ids.isEmpty()) return Map.of();
        var res = warehouseUseCase.getWarehousesByIds(ids);
        if (res == null) return Map.of();
        return res.entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, e -> WarehouseSummaryDto.builder().id(e.getValue().getId()).code(e.getValue().getCode()).name(e.getValue().getName()).build()));
    }

    private Map<UUID, LocationSummaryDto> buildLocationMap(List<StockMovement> items) {
        Set<UUID> ids = items.stream().map(StockMovement::getLocationId).filter(Objects::nonNull).collect(Collectors.toSet());
        if (ids.isEmpty()) return Map.of();
        var res = locationUseCase.getLocationsByIds(ids);
        if (res == null) return Map.of();
        return res.entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, e -> LocationSummaryDto.builder().id(e.getValue().getId()).code(e.getValue().getCode()).name(e.getValue().getName()).build()));
    }

    private Map<UUID, UserSummaryDto> buildUserMap(List<StockMovement> items) {
        Set<UUID> ids = items.stream().map(StockMovement::getCreatedBy).filter(Objects::nonNull).collect(Collectors.toSet());
        if (ids.isEmpty()) return Map.of();
        return ids.stream()
                .map(userRepository::findById)
                .filter(Objects::nonNull)
                .flatMap(Optional::stream)
                .collect(Collectors.toMap(User::getId, user -> UserSummaryDto.builder().id(user.getId()).username(user.getUsername()).fullName(user.getFullName()).build(), (u1, u2) -> u1));
    }
}
