package fpt.qn.mes.inventory.application.service;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import fpt.qn.mes.common.dto.response.PageResponse;
import fpt.qn.mes.common.dto.response.PaginationResult;
import fpt.qn.mes.inventory.application.dto.request.CreateMovementRequest;
import fpt.qn.mes.inventory.application.dto.request.CreateStockLotRequest;
import fpt.qn.mes.inventory.application.dto.response.StockBalanceDto;
import fpt.qn.mes.inventory.application.dto.response.StockLotDto;
import fpt.qn.mes.inventory.application.dto.response.StockMovementDto;
import fpt.qn.mes.inventory.application.exception.InsufficientStockException;
import fpt.qn.mes.inventory.application.exception.StockLotNotFoundException;
import fpt.qn.mes.inventory.application.mapper.InventoryDtoMapper;
import fpt.qn.mes.inventory.application.port.in.InventoryUseCase;
import fpt.qn.mes.inventory.domain.entities.StockBalance;
import fpt.qn.mes.inventory.domain.entities.StockLot;
import fpt.qn.mes.inventory.domain.entities.StockMovement;
import fpt.qn.mes.inventory.domain.repository.StockBalanceRepository;
import fpt.qn.mes.inventory.domain.repository.StockLotRepository;
import fpt.qn.mes.inventory.domain.repository.StockMovementRepository;
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
    InventoryDtoMapper mapper;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<StockLotDto> getStockLots(int page, int size) {
        PaginationResult<StockLot> result = lotRepository.findAll(page, size);
        List<StockLotDto> content = result.getItems().stream()
                .map(mapper::toDto)
                .toList();
        int totalPages = size > 0 ? (int) Math.ceil((double) result.getTotal() / size) : 0;
        return PageResponse.<StockLotDto>builder()
                .items(content)
                .totalElements(result.getTotal())
                .totalPages(totalPages)
                .pageNumber(page)
                .pageSize(size)
                .build();
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
    public PageResponse<StockMovementDto> getMovements(int page, int size) {
        PaginationResult<StockMovement> result = movementRepository.findAll(page, size);
        List<StockMovementDto> content = result.getItems().stream()
                .map(mapper::toDto)
                .toList();
        int totalPages = size > 0 ? (int) Math.ceil((double) result.getTotal() / size) : 0;
        return PageResponse.<StockMovementDto>builder()
                .items(content)
                .totalElements(result.getTotal())
                .totalPages(totalPages)
                .pageNumber(page)
                .pageSize(size)
                .build();
    }

    @Override
    @Transactional
    public StockMovementDto recordMovement(CreateMovementRequest request, UUID currentUserId) {
        if (request.getQuantity() == null || request.getQuantity().compareTo(BigDecimal.ZERO) <= 0) {
            throw new InsufficientStockException("Movement quantity must be positive");
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
                .id(optBalance.map(StockBalance::getId).orElse(UUID.randomUUID()))
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
        return mapper.toDto(savedMovement);
    }

    @Override
    @Transactional(readOnly = true)
    public List<StockBalanceDto> getStockBalances(UUID warehouseId, UUID productId) {
        List<StockBalance> balances = balanceRepository.findByWarehouseAndProduct(warehouseId, productId);
        return balances.stream()
                .map(mapper::toDto)
                .toList();
    }
}
