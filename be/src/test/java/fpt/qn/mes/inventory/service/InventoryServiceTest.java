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
import fpt.qn.mes.inventory.application.dto.request.StockBalanceSearchRequest;
import fpt.qn.mes.inventory.application.dto.request.StockInRequest;
import fpt.qn.mes.inventory.application.dto.request.StockMovementSearchRequest;
import fpt.qn.mes.inventory.application.dto.response.StockBalanceDto;
import fpt.qn.mes.inventory.application.dto.response.StockLotDto;
import fpt.qn.mes.inventory.application.dto.response.StockMovementDto;
import fpt.qn.mes.inventory.application.exception.InsufficientStockException;
import fpt.qn.mes.inventory.application.exception.StockLotNotFoundException;
import fpt.qn.mes.inventory.domain.repository.criteria.StockBalanceSearchCriteria;
import fpt.qn.mes.inventory.application.mapper.InventoryDtoMapper;
import fpt.qn.mes.inventory.application.service.InventoryService;
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
import fpt.qn.mes.inventory.domain.repository.criteria.StockLotSearchCriteria;
import fpt.qn.mes.inventory.domain.repository.criteria.StockMovementSearchCriteria;
import fpt.qn.mes.master.location.application.port.in.LocationUseCase;
import fpt.qn.mes.master.product.application.port.in.ProductUseCase;
import fpt.qn.mes.master.warehouse.application.port.in.WarehouseUseCase;

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

    @Mock
    MovementTypeRepository movementTypeRepository;

    @Mock
    StockStatusRepository stockStatusRepository;

    @Mock
    WarehouseUseCase warehouseUseCase;

    @Mock
    ProductUseCase productUseCase;

    @Mock
    LocationUseCase locationUseCase;

    @Mock
    fpt.qn.mes.user.domain.repository.UserRepository userRepository;

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
        when(mapper.toDto(any(StockMovement.class))).thenReturn(dto);

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
    @DisplayName("getStockBalances should return PageResponse of StockBalanceDto")
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

        StockBalanceSearchRequest request = new StockBalanceSearchRequest();
        request.setWarehouseId(warehouseId);
        request.setProductId(productId);

        when(balanceRepository.count(any(StockBalanceSearchCriteria.class))).thenReturn(1L);
        when(balanceRepository.search(any(StockBalanceSearchCriteria.class)))
                .thenReturn(List.of(balance));
        when(mapper.toDto(balance)).thenReturn(dto);

        PageResponse<StockBalanceDto> results = inventoryService.getStockBalances(request);

        assertThat(results).isNotNull();
        assertThat(results.getItems()).hasSize(1);
        assertThat(results.getItems().get(0).getQuantity()).isEqualTo(new BigDecimal("120.00"));
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

        when(movementRepository.count(any(StockMovementSearchCriteria.class))).thenReturn(1L);
        when(movementRepository.search(any(StockMovementSearchCriteria.class))).thenReturn(List.of(movement));
        when(mapper.toDto(any(StockMovement.class))).thenReturn(dto);

        StockMovementSearchRequest request = new StockMovementSearchRequest();
        request.setPage(0);
        request.setSize(10);
        PageResponse<StockMovementDto> response = inventoryService.getMovements(request);

        assertThat(response).isNotNull();
        assertThat(response.getItems()).hasSize(1);
    }

    @Test
    @DisplayName("recordStockIn should save new lot, balance, and movement when lot is new")
    void recordStockIn_NewLot_Success() {
        StockInRequest request = StockInRequest.builder()
                .productId(productId)
                .warehouseId(warehouseId)
                .locationId(locationId)
                .lotNumber("LOT-NEW-001")
                .quantity(new BigDecimal("100.00"))
                .referenceNo("PO-2026-001")
                .build();

        UUID availableStatusId = UUID.randomUUID();
        UUID purchaseInTypeId = UUID.randomUUID();

        when(lotRepository.findByLotNumber("LOT-NEW-001")).thenReturn(Optional.empty());
        when(stockStatusRepository.findIdByName(StockStatusConstants.AVAILABLE)).thenReturn(Optional.of(availableStatusId));
        when(movementTypeRepository.findIdByName(MovementTypeConstants.PURCHASE_IN)).thenReturn(Optional.of(purchaseInTypeId));
        when(balanceRepository.findForUpdate(warehouseId, locationId, productId, lotId)).thenReturn(Optional.empty());

        StockLot savedLot = StockLot.builder().id(lotId).lotNumber("LOT-NEW-001").productId(productId).build();
        when(lotRepository.save(any(StockLot.class))).thenReturn(savedLot);

        StockMovement savedMovement = StockMovement.builder()
                .id(UUID.randomUUID())
                .productId(productId)
                .quantity(new BigDecimal("100.00"))
                .referenceNo("PO-2026-001")
                .build();
        StockMovementDto dto = StockMovementDto.builder().id(savedMovement.getId()).quantity(new BigDecimal("100.00")).build();

        when(movementRepository.save(any(StockMovement.class))).thenReturn(savedMovement);
        when(mapper.toDto(any(StockMovement.class))).thenReturn(dto);

        StockMovementDto result = inventoryService.recordStockIn(request, userId);

        assertThat(result).isNotNull();
        assertThat(result.getQuantity()).isEqualTo(new BigDecimal("100.00"));
        verify(lotRepository).save(any(StockLot.class));
        verify(balanceRepository).save(any(StockBalance.class));
        verify(movementRepository).save(any(StockMovement.class));
    }

    @Test
    @DisplayName("recordStockIn should reuse existing lot when lot exists for the same product")
    void recordStockIn_ExistingLot_SameProduct_ReusesLot() {
        StockInRequest request = StockInRequest.builder()
                .productId(productId)
                .warehouseId(warehouseId)
                .locationId(locationId)
                .lotNumber("LOT-EXISTING-001")
                .quantity(new BigDecimal("50.00"))
                .referenceNo("PO-2026-002")
                .build();

        StockLot existingLot = StockLot.builder()
                .id(lotId)
                .lotNumber("LOT-EXISTING-001")
                .productId(productId)
                .build();

        UUID availableStatusId = UUID.randomUUID();
        UUID purchaseInTypeId = UUID.randomUUID();

        when(lotRepository.findByLotNumber("LOT-EXISTING-001")).thenReturn(Optional.of(existingLot));
        when(stockStatusRepository.findIdByName(StockStatusConstants.AVAILABLE)).thenReturn(Optional.of(availableStatusId));
        when(movementTypeRepository.findIdByName(MovementTypeConstants.PURCHASE_IN)).thenReturn(Optional.of(purchaseInTypeId));
        when(balanceRepository.findForUpdate(warehouseId, locationId, productId, lotId)).thenReturn(Optional.empty());

        StockMovement savedMovement = StockMovement.builder()
                .id(UUID.randomUUID())
                .productId(productId)
                .quantity(new BigDecimal("50.00"))
                .referenceNo("PO-2026-002")
                .build();
        StockMovementDto dto = StockMovementDto.builder().id(savedMovement.getId()).quantity(new BigDecimal("50.00")).build();

        when(movementRepository.save(any(StockMovement.class))).thenReturn(savedMovement);
        when(mapper.toDto(any(StockMovement.class))).thenReturn(dto);

        StockMovementDto result = inventoryService.recordStockIn(request, userId);

        assertThat(result).isNotNull();
        assertThat(result.getQuantity()).isEqualTo(new BigDecimal("50.00"));
        verify(balanceRepository).save(any(StockBalance.class));
        verify(movementRepository).save(any(StockMovement.class));
    }

    @Test
    @DisplayName("recordStockIn should throw InvalidStockLotException when existing lot belongs to different product")
    void recordStockIn_LotProductMismatch_ThrowsException() {
        StockInRequest request = StockInRequest.builder()
                .productId(productId)
                .warehouseId(warehouseId)
                .locationId(locationId)
                .lotNumber("LOT-MISMATCH")
                .quantity(new BigDecimal("50.00"))
                .referenceNo("PO-2026-002")
                .build();

        UUID existingProductOtherId = UUID.randomUUID();
        StockLot existingLot = StockLot.builder()
                .id(lotId)
                .lotNumber("LOT-MISMATCH")
                .productId(existingProductOtherId)
                .build();

        when(lotRepository.findByLotNumber("LOT-MISMATCH")).thenReturn(Optional.of(existingLot));

        assertThatThrownBy(() -> inventoryService.recordStockIn(request, userId))
                .isInstanceOf(fpt.qn.mes.inventory.application.exception.InvalidStockLotException.class)
                .hasMessageContaining("belongs to a different product");
    }

    @Test
    @DisplayName("createStockLot should throw StockLotConflictException when lot already exists for product")
    void createStockLot_Duplicate_ThrowsConflictException() {
        CreateStockLotRequest request = new CreateStockLotRequest();
        request.setLotNumber("LOT-EXISTS");
        request.setProductId(productId);

        StockLot existingLot = StockLot.builder()
                .id(lotId)
                .lotNumber("LOT-EXISTS")
                .productId(productId)
                .build();

        when(lotRepository.findByLotNumber("LOT-EXISTS")).thenReturn(Optional.of(existingLot));

        assertThatThrownBy(() -> inventoryService.createStockLot(request))
                .isInstanceOf(fpt.qn.mes.inventory.application.exception.StockLotConflictException.class)
                .hasMessageContaining("already exists");
    }
}
