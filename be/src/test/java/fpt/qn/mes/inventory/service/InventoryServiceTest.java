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
import fpt.qn.mes.inventory.application.dto.stockmovement.create.CreateStockMovementRequest;
import fpt.qn.mes.inventory.application.dto.stocklot.create.CreateStockLotRequest;
import fpt.qn.mes.inventory.application.dto.stockbalance.search.StockBalanceSearchRequest;
import fpt.qn.mes.inventory.application.dto.stockmovement.create.StockInRequest;
import fpt.qn.mes.inventory.application.dto.stockmovement.search.StockMovementSearchRequest;
import fpt.qn.mes.inventory.application.dto.stockbalance.StockBalanceResponse;
import fpt.qn.mes.inventory.application.dto.stocklot.StockLotResponse;
import fpt.qn.mes.inventory.application.dto.stockmovement.StockMovementResponse;
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
import fpt.qn.mes.inventory.domain.repository.LotTypeRepository;
import fpt.qn.mes.inventory.domain.repository.MovementTypeRepository;
import fpt.qn.mes.inventory.domain.repository.StockBalanceRepository;
import fpt.qn.mes.inventory.domain.repository.StockLotRepository;
import fpt.qn.mes.inventory.domain.repository.StockMovementRepository;
import fpt.qn.mes.inventory.domain.repository.StockStatusRepository;
import fpt.qn.mes.inventory.domain.repository.criteria.StockLotSearchCriteria;
import fpt.qn.mes.inventory.domain.repository.criteria.StockMovementSearchCriteria;
import fpt.qn.mes.master.location.application.dto.warehouselocation.WarehouseLocationResponse;
import fpt.qn.mes.master.location.application.port.in.WarehouseLocationUseCase;
import fpt.qn.mes.master.product.application.port.in.ProductUseCase;
import fpt.qn.mes.master.warehouse.application.port.in.WarehouseUseCase;

@ExtendWith(MockitoExtension.class)
class InventoryServiceTest {

    @Mock
    StockLotRepository lotRepository;

    @Mock
    LotTypeRepository lotTypeRepository;

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
    WarehouseLocationUseCase warehouseLocationUseCase;

    @Mock
    fpt.qn.mes.user.domain.repository.UserRepository userRepository;

    @Mock
    fpt.qn.mes.user.application.mapper.UserDtoMapper userDtoMapper;

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

        StockLotResponse expectedDto = StockLotResponse.builder()
                .id(lotId)
                .lotNumber("LOT-2026-001")
                .productId(productId)
                .build();

        when(lotRepository.save(any(StockLot.class))).thenReturn(savedLot);

        inventoryService.createStockLot(request);

        verify(lotRepository).save(any(StockLot.class));
    }

    @Test
    @DisplayName("getStockLotById should return DTO when lot exists")
    void getStockLotById_Success() {
        StockLot lot = StockLot.builder().id(lotId).lotNumber("LOT-001").build();
        StockLotResponse dto = StockLotResponse.builder().id(lotId).lotNumber("LOT-001").build();

        when(lotRepository.findById(lotId)).thenReturn(Optional.of(lot));
        when(mapper.toDto(lot)).thenReturn(dto);

        StockLotResponse result = inventoryService.getStockLotById(lotId);

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
        CreateStockMovementRequest request = new CreateStockMovementRequest();
        request.setMovementTypeId(UUID.randomUUID());
        request.setProductId(productId);
        request.setWarehouseId(warehouseId);
        request.setLocationId(locationId);
        request.setQuantity(new BigDecimal("50.00"));
        request.setToStatusId(UUID.randomUUID());
        request.setReferenceNo("PO-10001");

        when(balanceRepository.findForUpdate(warehouseId, locationId, productId, null, request.getToStatusId()))
                .thenReturn(Optional.empty());

        StockMovement savedMovement = StockMovement.builder()
                .id(UUID.randomUUID())
                .productId(productId)
                .quantity(new BigDecimal("50.00"))
                .referenceNo("PO-10001")
                .build();

        StockMovementResponse dto = StockMovementResponse.builder()
                .id(savedMovement.getId())
                .quantity(new BigDecimal("50.00"))
                .referenceNo("PO-10001")
                .build();

        when(movementRepository.save(any(StockMovement.class))).thenReturn(savedMovement);

        inventoryService.recordMovement(request, userId);

        verify(balanceRepository).save(any(StockBalance.class));
        verify(movementRepository).save(any(StockMovement.class));
    }

    @Test
    @DisplayName("recordMovement for ISSUE with insufficient stock should throw InsufficientStockException")
    void recordMovement_Issue_InsufficientStock() {
        CreateStockMovementRequest request = new CreateStockMovementRequest();
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

        when(balanceRepository.findForUpdate(warehouseId, locationId, productId, null, request.getFromStatusId()))
                .thenReturn(Optional.of(existingBalance));

        assertThatThrownBy(() -> inventoryService.recordMovement(request, userId))
                .isInstanceOf(InsufficientStockException.class)
                .hasMessageContaining("Insufficient stock balance");
    }

    @Test
    @DisplayName("getStockBalances should return PageResponse of StockBalanceResponse")
    void getStockBalances_Success() {
        StockBalance balance = StockBalance.builder()
                .id(UUID.randomUUID())
                .warehouseId(warehouseId)
                .productId(productId)
                .quantity(new BigDecimal("120.00"))
                .build();

        StockBalanceResponse dto = StockBalanceResponse.builder()
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

        PageResponse<StockBalanceResponse> results = inventoryService.getStockBalances(request);

        assertThat(results).isNotNull();
        assertThat(results.getItems()).hasSize(1);
        assertThat(results.getItems().get(0).getQuantity()).isEqualTo(new BigDecimal("120.00"));
    }

    @Test
    @DisplayName("getMovements should return paginated StockMovementResponse")
    void getMovements_Success() {
        StockMovement movement = StockMovement.builder()
                .id(UUID.randomUUID())
                .productId(productId)
                .quantity(new BigDecimal("10.00"))
                .build();

        StockMovementResponse dto = StockMovementResponse.builder()
                .id(movement.getId())
                .quantity(new BigDecimal("10.00"))
                .build();

        when(movementRepository.count(any(StockMovementSearchCriteria.class))).thenReturn(1L);
        when(movementRepository.search(any(StockMovementSearchCriteria.class))).thenReturn(List.of(movement));
        when(mapper.toDto(any(StockMovement.class))).thenReturn(dto);

        StockMovementSearchRequest request = new StockMovementSearchRequest();
        request.setPage(0);
        request.setSize(10);
        PageResponse<StockMovementResponse> response = inventoryService.getMovements(request);

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
        when(balanceRepository.findForUpdate(warehouseId, locationId, productId, lotId, availableStatusId)).thenReturn(Optional.empty());

        StockLot savedLot = StockLot.builder().id(lotId).lotNumber("LOT-NEW-001").productId(productId).build();
        when(lotRepository.save(any(StockLot.class))).thenReturn(savedLot);

        StockMovement savedMovement = StockMovement.builder()
                .id(UUID.randomUUID())
                .productId(productId)
                .quantity(new BigDecimal("100.00"))
                .referenceNo("PO-2026-001")
                .build();
        StockMovementResponse dto = StockMovementResponse.builder().id(savedMovement.getId()).quantity(new BigDecimal("100.00")).build();

        when(movementRepository.save(any(StockMovement.class))).thenReturn(savedMovement);

        inventoryService.recordStockIn(request, userId);

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
        when(balanceRepository.findForUpdate(warehouseId, locationId, productId, lotId, availableStatusId)).thenReturn(Optional.empty());

        StockMovement savedMovement = StockMovement.builder()
                .id(UUID.randomUUID())
                .productId(productId)
                .quantity(new BigDecimal("50.00"))
                .referenceNo("PO-2026-002")
                .build();
        StockMovementResponse dto = StockMovementResponse.builder().id(savedMovement.getId()).quantity(new BigDecimal("50.00")).build();

        when(movementRepository.save(any(StockMovement.class))).thenReturn(savedMovement);

        inventoryService.recordStockIn(request, userId);

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

    @Test
    @DisplayName("transferStock should validate non-positive quantity")
    void transferStock_NonPositiveQuantity_ThrowsException() {
        var request = fpt.qn.mes.inventory.application.dto.stockmovement.create.StockTransferRequest.builder()
                .fromWarehouseId(UUID.randomUUID())
                .fromLocationId(UUID.randomUUID())
                .toWarehouseId(UUID.randomUUID())
                .toLocationId(UUID.randomUUID())
                .productId(UUID.randomUUID())
                .lotId(UUID.randomUUID())
                .quantity(BigDecimal.ZERO)
                .build();

        assertThatThrownBy(() -> inventoryService.transferStock(request, UUID.randomUUID()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Quantity must be greater than zero");
    }

    @Test
    @DisplayName("transferStock should validate same location transfer")
    void transferStock_SameLocation_ThrowsException() {
        UUID locId = UUID.randomUUID();
        var request = fpt.qn.mes.inventory.application.dto.stockmovement.create.StockTransferRequest.builder()
                .fromWarehouseId(UUID.randomUUID())
                .fromLocationId(locId)
                .toWarehouseId(UUID.randomUUID())
                .toLocationId(locId)
                .productId(UUID.randomUUID())
                .lotId(UUID.randomUUID())
                .quantity(BigDecimal.TEN)
                .build();

        assertThatThrownBy(() -> inventoryService.transferStock(request, UUID.randomUUID()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("cannot be the same");
    }

    @Test
    @DisplayName("transferStock should validate insufficient source balance")
    void transferStock_InsufficientStock_ThrowsInsufficientStockException() {
        UUID fromWh = UUID.randomUUID();
        UUID fromLoc = UUID.randomUUID();
        UUID toWh = UUID.randomUUID();
        UUID toLoc = UUID.randomUUID();
        UUID prodId = UUID.randomUUID();
        UUID lotId = UUID.randomUUID();
        UUID statusId = UUID.randomUUID();

        var request = fpt.qn.mes.inventory.application.dto.stockmovement.create.StockTransferRequest.builder()
                .fromWarehouseId(fromWh)
                .fromLocationId(fromLoc)
                .toWarehouseId(toWh)
                .toLocationId(toLoc)
                .productId(prodId)
                .lotId(lotId)
                .quantity(new BigDecimal("50.00"))
                .build();

        fpt.qn.mes.inventory.domain.entities.StockStatus availStatus = fpt.qn.mes.inventory.domain.entities.StockStatus.builder()
                .id(statusId)
                .name(StockStatusConstants.AVAILABLE)
                .build();

        when(stockStatusRepository.findIdByName(StockStatusConstants.AVAILABLE)).thenReturn(Optional.of(statusId));
        when(movementTypeRepository.findIdByName(MovementTypeConstants.TRANSFER_OUT)).thenReturn(Optional.of(UUID.randomUUID()));
        when(movementTypeRepository.findIdByName(MovementTypeConstants.TRANSFER_IN)).thenReturn(Optional.of(UUID.randomUUID()));

        when(warehouseLocationUseCase.getWarehouseLocationById(fromLoc)).thenReturn(WarehouseLocationResponse.builder().id(fromLoc).warehouse(WarehouseLocationResponse.WarehouseInfo.builder().id(fromWh).build()).code("WH1/A01").build());
        when(warehouseLocationUseCase.getWarehouseLocationById(toLoc)).thenReturn(WarehouseLocationResponse.builder().id(toLoc).warehouse(WarehouseLocationResponse.WarehouseInfo.builder().id(toWh).build()).code("WH1/A02").build());

        StockBalance sourceBal = StockBalance.builder()
                .id(UUID.randomUUID())
                .warehouseId(fromWh)
                .locationId(fromLoc)
                .productId(prodId)
                .lotId(lotId)
                .stockStatusId(statusId)
                .quantity(new BigDecimal("20.00"))
                .build();

        when(balanceRepository.findForUpdate(fromWh, fromLoc, prodId, lotId, statusId))
                .thenReturn(Optional.of(sourceBal));

        assertThatThrownBy(() -> inventoryService.transferStock(request, UUID.randomUUID()))
                .isInstanceOf(InsufficientStockException.class);
    }

    @Test
    @DisplayName("transferStock should deduct source, increase destination, and record paired movements")
    void transferStock_ValidRequest_Success() {
        UUID fromWh = UUID.randomUUID();
        UUID fromLoc = UUID.randomUUID();
        UUID toWh = UUID.randomUUID();
        UUID toLoc = UUID.randomUUID();
        UUID prodId = UUID.randomUUID();
        UUID lotId = UUID.randomUUID();
        UUID statusId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        var request = fpt.qn.mes.inventory.application.dto.stockmovement.create.StockTransferRequest.builder()
                .fromWarehouseId(fromWh)
                .fromLocationId(fromLoc)
                .toWarehouseId(toWh)
                .toLocationId(toLoc)
                .productId(prodId)
                .lotId(lotId)
                .quantity(new BigDecimal("20.00"))
                .build();

        when(stockStatusRepository.findIdByName(StockStatusConstants.AVAILABLE)).thenReturn(Optional.of(statusId));
        when(movementTypeRepository.findIdByName(MovementTypeConstants.TRANSFER_OUT)).thenReturn(Optional.of(UUID.randomUUID()));
        when(movementTypeRepository.findIdByName(MovementTypeConstants.TRANSFER_IN)).thenReturn(Optional.of(UUID.randomUUID()));

        when(warehouseLocationUseCase.getWarehouseLocationById(fromLoc)).thenReturn(WarehouseLocationResponse.builder().id(fromLoc).warehouse(WarehouseLocationResponse.WarehouseInfo.builder().id(fromWh).build()).code("WH1/A01").build());
        when(warehouseLocationUseCase.getWarehouseLocationById(toLoc)).thenReturn(WarehouseLocationResponse.builder().id(toLoc).warehouse(WarehouseLocationResponse.WarehouseInfo.builder().id(toWh).build()).code("WH1/A02").build());

        StockBalance sourceBal = StockBalance.builder()
                .id(UUID.randomUUID())
                .warehouseId(fromWh)
                .locationId(fromLoc)
                .productId(prodId)
                .lotId(lotId)
                .stockStatusId(statusId)
                .quantity(new BigDecimal("50.00"))
                .build();

        StockBalance destBal = StockBalance.builder()
                .id(UUID.randomUUID())
                .warehouseId(toWh)
                .locationId(toLoc)
                .productId(prodId)
                .lotId(lotId)
                .stockStatusId(statusId)
                .quantity(new BigDecimal("10.00"))
                .build();

        when(balanceRepository.findForUpdate(fromWh, fromLoc, prodId, lotId, statusId)).thenReturn(Optional.of(sourceBal));
        when(balanceRepository.findForUpdate(toWh, toLoc, prodId, lotId, statusId)).thenReturn(Optional.of(destBal));
        when(balanceRepository.save(any(StockBalance.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(movementRepository.save(any(StockMovement.class))).thenAnswer(invocation -> invocation.getArgument(0));

        fpt.qn.mes.inventory.application.dto.response.StockTransferResponse response = inventoryService.transferStock(request, userId);

        assertThat(response).isNotNull();
        assertThat(sourceBal.getQuantity()).isEqualByComparingTo(new BigDecimal("30.00"));
        assertThat(destBal.getQuantity()).isEqualByComparingTo(new BigDecimal("30.00"));
        verify(movementRepository, org.mockito.Mockito.times(2)).save(any(StockMovement.class));
    }
}
