package fpt.qn.mes.inventory.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import fpt.qn.mes.common.dto.response.PageResponse;
import fpt.qn.mes.inventory.application.dto.request.CreateMovementRequest;
import fpt.qn.mes.inventory.application.dto.request.CreateStockLotRequest;
import fpt.qn.mes.inventory.application.dto.response.StockBalanceDto;
import fpt.qn.mes.inventory.application.dto.response.StockLotDto;
import fpt.qn.mes.inventory.application.dto.response.StockMovementDto;
import fpt.qn.mes.inventory.application.exception.InsufficientStockException;
import fpt.qn.mes.inventory.application.exception.StockLotNotFoundException;
import fpt.qn.mes.inventory.application.mapper.InventoryDtoMapper;
import fpt.qn.mes.inventory.application.service.InventoryService;
import fpt.qn.mes.inventory.domain.entities.StockBalance;
import fpt.qn.mes.inventory.domain.entities.StockLot;
import fpt.qn.mes.inventory.domain.entities.StockMovement;
import fpt.qn.mes.inventory.domain.repository.StockBalanceRepository;
import fpt.qn.mes.inventory.domain.repository.StockLotRepository;
import fpt.qn.mes.inventory.domain.repository.StockMovementRepository;
import fpt.qn.mes.inventory.domain.repository.criteria.StockLotSearchCriteria;
import fpt.qn.mes.inventory.domain.repository.criteria.StockMovementSearchCriteria;

@ExtendWith(MockitoExtension.class)
class InventoryServiceTest {

    @Mock
    StockLotRepository lotRepository;

    @Mock
    StockMovementRepository movementRepository;

    @Mock
    StockBalanceRepository balanceRepository;

    @Mock
    InventoryDtoMapper mapper;

    @InjectMocks
    InventoryService inventoryService;

    UUID productId;
    UUID warehouseId;
    UUID locationId;
    UUID lotId;
    UUID userId;

    @BeforeEach
    void setUp() {
        productId = UUID.randomUUID();
        warehouseId = UUID.randomUUID();
        locationId = UUID.randomUUID();
        lotId = UUID.randomUUID();
        userId = UUID.randomUUID();
    }

    @Test
    @DisplayName("createStockLot should save lot and return DTO")
    void createStockLot_Success() {
        CreateStockLotRequest request = new CreateStockLotRequest();
        request.setLotNumber("LOT-2026-001");
        request.setProductId(productId);
        request.setExpiryDate(LocalDate.now().plusMonths(6));

        StockLot savedLot = StockLot.builder()
                .id(lotId)
                .lotNumber("LOT-2026-001")
                .productId(productId)
                .expiryDate(request.getExpiryDate())
                .build();

        StockLotDto expectedDto = StockLotDto.builder()
                .id(lotId)
                .lotNumber("LOT-2026-001")
                .productId(productId)
                .build();

        when(lotRepository.save(any(StockLot.class))).thenReturn(savedLot);
        when(mapper.toDto(savedLot)).thenReturn(expectedDto);

        StockLotDto result = inventoryService.createStockLot(request);

        assertThat(result).isNotNull();
        assertThat(result.getLotNumber()).isEqualTo("LOT-2026-001");
        verify(lotRepository).save(any(StockLot.class));
    }

    @Test
    @DisplayName("getStockLotById should return DTO when lot exists")
    void getStockLotById_Success() {
        StockLot lot = StockLot.builder().id(lotId).lotNumber("LOT-001").build();
        StockLotDto dto = StockLotDto.builder().id(lotId).lotNumber("LOT-001").build();

        when(lotRepository.findById(lotId)).thenReturn(Optional.of(lot));
        when(mapper.toDto(lot)).thenReturn(dto);

        StockLotDto result = inventoryService.getStockLotById(lotId);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(lotId);
    }

    @Test
    @DisplayName("getStockLotById should throw StockLotNotFoundException when lot does not exist")
    void getStockLotById_NotFound() {
        when(lotRepository.findById(lotId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> inventoryService.getStockLotById(lotId))
                .isInstanceOf(StockLotNotFoundException.class)
                .hasMessageContaining("Stock lot not found with ID");
    }

    @Test
    @DisplayName("recordMovement for RECEIPT should increase stock balance and log movement")
    void recordMovement_Receipt_Success() {
        CreateMovementRequest request = new CreateMovementRequest();
        request.setMovementTypeId(UUID.randomUUID());
        request.setProductId(productId);
        request.setWarehouseId(warehouseId);
        request.setLocationId(locationId);
        request.setQuantity(new BigDecimal("50.00"));
        request.setToStatusId(UUID.randomUUID());
        request.setReferenceNo("PO-10001");

        when(balanceRepository.findForUpdate(warehouseId, locationId, productId, null))
                .thenReturn(Optional.empty());

        StockMovement savedMovement = StockMovement.builder()
                .id(UUID.randomUUID())
                .productId(productId)
                .quantity(new BigDecimal("50.00"))
                .referenceNo("PO-10001")
                .build();

        StockMovementDto dto = StockMovementDto.builder()
                .id(savedMovement.getId())
                .quantity(new BigDecimal("50.00"))
                .referenceNo("PO-10001")
                .build();

        when(movementRepository.save(any(StockMovement.class))).thenReturn(savedMovement);
        when(mapper.toDto(savedMovement)).thenReturn(dto);

        StockMovementDto result = inventoryService.recordMovement(request, userId);

        assertThat(result).isNotNull();
        assertThat(result.getQuantity()).isEqualTo(new BigDecimal("50.00"));
        verify(balanceRepository).save(any(StockBalance.class));
        verify(movementRepository).save(any(StockMovement.class));
    }

    @Test
    @DisplayName("recordMovement for ISSUE with insufficient stock should throw InsufficientStockException")
    void recordMovement_Issue_InsufficientStock() {
        CreateMovementRequest request = new CreateMovementRequest();
        request.setMovementTypeId(UUID.randomUUID());
        request.setProductId(productId);
        request.setWarehouseId(warehouseId);
        request.setLocationId(locationId);
        request.setQuantity(new BigDecimal("100.00"));
        request.setFromStatusId(UUID.randomUUID());

        StockBalance existingBalance = StockBalance.builder()
                .id(UUID.randomUUID())
                .warehouseId(warehouseId)
                .locationId(locationId)
                .productId(productId)
                .quantity(new BigDecimal("30.00"))
                .build();

        when(balanceRepository.findForUpdate(warehouseId, locationId, productId, null))
                .thenReturn(Optional.of(existingBalance));

        assertThatThrownBy(() -> inventoryService.recordMovement(request, userId))
                .isInstanceOf(InsufficientStockException.class)
                .hasMessageContaining("Insufficient stock balance");
    }

    @Test
    @DisplayName("getStockBalances should return mapped list of StockBalanceDto")
    void getStockBalances_Success() {
        StockBalance balance = StockBalance.builder()
                .id(UUID.randomUUID())
                .warehouseId(warehouseId)
                .productId(productId)
                .quantity(new BigDecimal("120.00"))
                .build();

        StockBalanceDto dto = StockBalanceDto.builder()
                .id(balance.getId())
                .warehouseId(warehouseId)
                .productId(productId)
                .quantity(new BigDecimal("120.00"))
                .build();

        when(balanceRepository.findByWarehouseAndProduct(warehouseId, productId))
                .thenReturn(List.of(balance));
        when(mapper.toDto(balance)).thenReturn(dto);

        List<StockBalanceDto> results = inventoryService.getStockBalances(warehouseId, productId);

        assertThat(results).hasSize(1);
        assertThat(results.get(0).getQuantity()).isEqualTo(new BigDecimal("120.00"));
    }

    @Test
    @DisplayName("getMovements should return paginated StockMovementDto")
    void getMovements_Success() {
        StockMovement movement = StockMovement.builder()
                .id(UUID.randomUUID())
                .productId(productId)
                .quantity(new BigDecimal("10.00"))
                .build();

        StockMovementDto dto = StockMovementDto.builder()
                .id(movement.getId())
                .quantity(new BigDecimal("10.00"))
                .build();

        PageResponse<StockMovement> pageResponse = PageResponse.<StockMovement>builder()
                .items(List.of(movement))
                .totalElements(1L)
                .totalPages(1)
                .pageNumber(0)
                .pageSize(10)
                .build();

        when(movementRepository.search(any(StockMovementSearchCriteria.class))).thenReturn(pageResponse);
        when(mapper.toDto(movement)).thenReturn(dto);

        PageResponse<StockMovementDto> response = inventoryService.getMovements(0, 10);

        assertThat(response).isNotNull();
        assertThat(response.getItems()).hasSize(1);
    }
}
