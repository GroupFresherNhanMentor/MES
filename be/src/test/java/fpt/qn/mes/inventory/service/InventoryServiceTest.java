package fpt.qn.mes.inventory.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
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

import fpt.qn.mes.auth.application.port.out.CurrentUserPort;
import fpt.qn.mes.common.domainQuery.PaginationResult;
import fpt.qn.mes.common.dto.response.PageResponse;
import fpt.qn.mes.common.exception.DomainException;
import fpt.qn.mes.inventory.application.dto.stockbalance.StockBalanceResponse;
import fpt.qn.mes.inventory.application.dto.stockbalance.search.StockBalanceSearchRequest;
import fpt.qn.mes.inventory.application.dto.stockmovement.StockMovementResponse;
import fpt.qn.mes.inventory.application.dto.stockmovement.create.CreateStockMovementRequest;
import fpt.qn.mes.inventory.application.dto.stockmovement.create.StockInRequest;
import fpt.qn.mes.inventory.application.dto.stockmovement.create.StockTransferRequest;
import fpt.qn.mes.inventory.application.dto.stockmovement.search.StockMovementSearchRequest;
import fpt.qn.mes.inventory.application.exception.InsufficientStockException;
import fpt.qn.mes.inventory.application.exception.InvalidStockLotException;
import fpt.qn.mes.inventory.application.exception.InvalidStockTransferException;
import fpt.qn.mes.inventory.application.mapper.InventoryDtoMapper;
import fpt.qn.mes.inventory.application.mapper.StockAdjustmentApprovalDtoMapper;
import fpt.qn.mes.inventory.application.port.out.ProductCheckPort;
import fpt.qn.mes.inventory.application.port.out.WarehouseCheckPort;
import fpt.qn.mes.inventory.application.port.out.WarehouseLocationCheckPort;
import fpt.qn.mes.inventory.application.port.out.WarehouseLocationQueryPort;
import fpt.qn.mes.inventory.application.service.InventoryService;
import fpt.qn.mes.inventory.domain.constants.MovementTypeConstants;
import fpt.qn.mes.inventory.domain.constants.StockStatusConstants;
import fpt.qn.mes.inventory.domain.entities.StockBalance;
import fpt.qn.mes.inventory.domain.entities.StockLot;
import fpt.qn.mes.inventory.domain.entities.StockMovement;
import fpt.qn.mes.inventory.domain.repository.MovementTypeRepository;
import fpt.qn.mes.inventory.domain.repository.StockAdjustmentApprovalRepository;
import fpt.qn.mes.inventory.domain.repository.StockBalanceRepository;
import fpt.qn.mes.inventory.domain.repository.StockLotRepository;
import fpt.qn.mes.inventory.domain.repository.StockMovementRepository;
import fpt.qn.mes.inventory.domain.repository.StockStatusRepository;
import fpt.qn.mes.inventory.domain.repository.criteria.StockBalanceSearchCriteria;
import fpt.qn.mes.inventory.domain.repository.criteria.StockMovementSearchCriteria;

@ExtendWith(MockitoExtension.class)
class InventoryServiceTest {

    @Mock StockLotRepository lotRepository;
    @Mock StockMovementRepository movementRepository;
    @Mock StockBalanceRepository balanceRepository;
    @Mock MovementTypeRepository movementTypeRepository;
    @Mock StockStatusRepository stockStatusRepository;
    @Mock StockAdjustmentApprovalRepository approvalRepository;
    @Mock InventoryDtoMapper mapper;
    @Mock StockAdjustmentApprovalDtoMapper approvalMapper;
    @Mock CurrentUserPort currentUserPort;
    @Mock ProductCheckPort productCheckPort;
    @Mock WarehouseCheckPort warehouseCheckPort;
    @Mock WarehouseLocationCheckPort warehouseLocationCheckPort;
    @Mock WarehouseLocationQueryPort warehouseLocationQueryPort;

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

        when(productCheckPort.existsById(productId)).thenReturn(true);
        when(warehouseCheckPort.existsById(warehouseId)).thenReturn(true);
        when(warehouseLocationCheckPort.existsById(locationId)).thenReturn(true);
        when(currentUserPort.getCurrentUserId()).thenReturn(userId);
        when(balanceRepository.findForUpdate(warehouseId, locationId, productId, null, request.getToStatusId()))
                .thenReturn(Optional.empty());
        when(movementRepository.save(any(StockMovement.class)))
                .thenAnswer(i -> i.getArgument(0));

        inventoryService.recordMovement(request);

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

        when(productCheckPort.existsById(productId)).thenReturn(true);
        when(warehouseCheckPort.existsById(warehouseId)).thenReturn(true);
        when(warehouseLocationCheckPort.existsById(locationId)).thenReturn(true);

        StockBalance existingBalance = StockBalance.builder()
                .id(UUID.randomUUID())
                .warehouse(StockBalance.WarehouseRef.builder().id(warehouseId).build())
                .location(StockBalance.LocationRef.builder().id(locationId).build())
                .product(StockBalance.ProductRef.builder().id(productId).build())
                .quantity(new BigDecimal("30.00"))
                .build();

        when(balanceRepository.findForUpdate(warehouseId, locationId, productId, null, request.getFromStatusId()))
                .thenReturn(Optional.of(existingBalance));

        assertThatThrownBy(() -> inventoryService.recordMovement(request))
                .isInstanceOf(InsufficientStockException.class)
                .hasMessageContaining("Insufficient stock balance");
    }

    @Test
    @DisplayName("getStockBalances should return PageResponse of StockBalanceResponse")
    void getStockBalances_Success() {
        StockBalance balance = StockBalance.builder()
                .id(UUID.randomUUID())
                .warehouse(StockBalance.WarehouseRef.builder().id(warehouseId).build())
                .product(StockBalance.ProductRef.builder().id(productId).build())
                .quantity(new BigDecimal("120.00"))
                .build();

        StockBalanceResponse dto = StockBalanceResponse.builder()
                .id(balance.getId())
                .warehouse(StockBalanceResponse.WarehouseInfo.builder().id(warehouseId).build())
                .product(StockBalanceResponse.ProductInfo.builder().id(productId).build())
                .quantity(new BigDecimal("120.00"))
                .build();

        StockBalanceSearchRequest request = new StockBalanceSearchRequest();
        request.setWarehouseId(warehouseId);
        request.setProductId(productId);

        when(balanceRepository.search(any(StockBalanceSearchCriteria.class)))
                .thenReturn(PaginationResult.<StockBalance>builder().total(1L).items(List.of(balance)).build());
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

        when(movementRepository.search(any(StockMovementSearchCriteria.class)))
                .thenReturn(PaginationResult.<StockMovement>builder().total(1L).items(List.of(movement)).build());
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

        when(productCheckPort.existsById(productId)).thenReturn(true);
        when(warehouseCheckPort.existsById(warehouseId)).thenReturn(true);
        when(warehouseLocationCheckPort.existsById(locationId)).thenReturn(true);
        when(currentUserPort.getCurrentUserId()).thenReturn(userId);
        when(lotRepository.findByLotNumber("LOT-NEW-001")).thenReturn(Optional.empty());
        when(stockStatusRepository.findIdByName(StockStatusConstants.AVAILABLE)).thenReturn(Optional.of(availableStatusId));
        when(movementTypeRepository.findIdByName(MovementTypeConstants.PURCHASE_IN)).thenReturn(Optional.of(purchaseInTypeId));

        StockLot savedLot = StockLot.builder().id(lotId).lotNumber("LOT-NEW-001")
                .product(StockLot.ProductRef.builder().id(productId).build()).build();
        when(lotRepository.save(any(StockLot.class))).thenReturn(savedLot);

        when(balanceRepository.findForUpdate(warehouseId, locationId, productId, lotId, availableStatusId))
                .thenReturn(Optional.empty());
        when(movementRepository.save(any(StockMovement.class))).thenAnswer(i -> i.getArgument(0));

        inventoryService.recordStockIn(request);

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
                .product(StockLot.ProductRef.builder().id(productId).build())
                .build();

        UUID availableStatusId = UUID.randomUUID();
        UUID purchaseInTypeId = UUID.randomUUID();

        when(productCheckPort.existsById(productId)).thenReturn(true);
        when(warehouseCheckPort.existsById(warehouseId)).thenReturn(true);
        when(warehouseLocationCheckPort.existsById(locationId)).thenReturn(true);
        when(currentUserPort.getCurrentUserId()).thenReturn(userId);
        when(lotRepository.findByLotNumber("LOT-EXISTING-001")).thenReturn(Optional.of(existingLot));
        when(stockStatusRepository.findIdByName(StockStatusConstants.AVAILABLE)).thenReturn(Optional.of(availableStatusId));
        when(movementTypeRepository.findIdByName(MovementTypeConstants.PURCHASE_IN)).thenReturn(Optional.of(purchaseInTypeId));
        when(balanceRepository.findForUpdate(warehouseId, locationId, productId, lotId, availableStatusId))
                .thenReturn(Optional.empty());
        when(movementRepository.save(any(StockMovement.class))).thenAnswer(i -> i.getArgument(0));

        inventoryService.recordStockIn(request);

        verify(lotRepository, never()).save(any());
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

        StockLot existingLot = StockLot.builder()
                .id(lotId)
                .lotNumber("LOT-MISMATCH")
                .product(StockLot.ProductRef.builder().id(UUID.randomUUID()).build())
                .build();

        when(productCheckPort.existsById(productId)).thenReturn(true);
        when(warehouseCheckPort.existsById(warehouseId)).thenReturn(true);
        when(warehouseLocationCheckPort.existsById(locationId)).thenReturn(true);
        when(lotRepository.findByLotNumber("LOT-MISMATCH")).thenReturn(Optional.of(existingLot));

        assertThatThrownBy(() -> inventoryService.recordStockIn(request))
                .isInstanceOf(InvalidStockLotException.class)
                .hasMessageContaining("belongs to a different product");
    }

    @Test
    @DisplayName("transferStock should validate non-positive quantity")
    void transferStock_NonPositiveQuantity_ThrowsException() {
        UUID fromWh = UUID.randomUUID(), fromLoc = UUID.randomUUID();
        UUID toWh = UUID.randomUUID(), toLoc = UUID.randomUUID();
        UUID prodId = UUID.randomUUID(), tLotId = UUID.randomUUID();
        UUID statusId = UUID.randomUUID();

        StockTransferRequest request = StockTransferRequest.builder()
                .fromWarehouseId(fromWh).fromLocationId(fromLoc)
                .toWarehouseId(toWh).toLocationId(toLoc)
                .productId(prodId).lotId(tLotId)
                .quantity(BigDecimal.ZERO)
                .build();

        when(warehouseLocationQueryPort.belongsToWarehouse(fromLoc, fromWh)).thenReturn(true);
        when(warehouseLocationQueryPort.belongsToWarehouse(toLoc, toWh)).thenReturn(true);
        when(stockStatusRepository.findIdByName(StockStatusConstants.AVAILABLE)).thenReturn(Optional.of(statusId));
        when(movementTypeRepository.findIdByName(MovementTypeConstants.TRANSFER_OUT)).thenReturn(Optional.of(UUID.randomUUID()));
        when(movementTypeRepository.findIdByName(MovementTypeConstants.TRANSFER_IN)).thenReturn(Optional.of(UUID.randomUUID()));

        StockBalance sourceBal = StockBalance.builder()
                .id(UUID.randomUUID())
                .warehouse(StockBalance.WarehouseRef.builder().id(fromWh).build())
                .location(StockBalance.LocationRef.builder().id(fromLoc).build())
                .product(StockBalance.ProductRef.builder().id(prodId).build())
                .lot(StockBalance.LotRef.builder().id(tLotId).build())
                .stockStatus(StockBalance.StockStatusRef.builder().id(statusId).build())
                .quantity(new BigDecimal("50.00"))
                .build();
        when(balanceRepository.findForUpdate(fromWh, fromLoc, prodId, tLotId, statusId)).thenReturn(Optional.of(sourceBal));

        assertThatThrownBy(() -> inventoryService.transferStock(request))
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("Deduction quantity must be positive");
    }

    @Test
    @DisplayName("transferStock should validate same location transfer")
    void transferStock_SameLocation_ThrowsException() {
        UUID locId = UUID.randomUUID();
        StockTransferRequest request = StockTransferRequest.builder()
                .fromWarehouseId(UUID.randomUUID())
                .fromLocationId(locId)
                .toWarehouseId(UUID.randomUUID())
                .toLocationId(locId)
                .productId(UUID.randomUUID())
                .lotId(UUID.randomUUID())
                .quantity(BigDecimal.TEN)
                .build();

        assertThatThrownBy(() -> inventoryService.transferStock(request))
                .isInstanceOf(InvalidStockTransferException.class)
                .hasMessageContaining("cannot be the same");
    }

    @Test
    @DisplayName("transferStock should throw InsufficientStockException when source balance is too low")
    void transferStock_InsufficientStock_ThrowsInsufficientStockException() {
        UUID fromWh = UUID.randomUUID();
        UUID fromLoc = UUID.randomUUID();
        UUID toWh = UUID.randomUUID();
        UUID toLoc = UUID.randomUUID();
        UUID prodId = UUID.randomUUID();
        UUID tLotId = UUID.randomUUID();
        UUID statusId = UUID.randomUUID();

        StockTransferRequest request = StockTransferRequest.builder()
                .fromWarehouseId(fromWh)
                .fromLocationId(fromLoc)
                .toWarehouseId(toWh)
                .toLocationId(toLoc)
                .productId(prodId)
                .lotId(tLotId)
                .quantity(new BigDecimal("50.00"))
                .build();

        when(warehouseLocationQueryPort.belongsToWarehouse(fromLoc, fromWh)).thenReturn(true);
        when(warehouseLocationQueryPort.belongsToWarehouse(toLoc, toWh)).thenReturn(true);
        when(stockStatusRepository.findIdByName(StockStatusConstants.AVAILABLE)).thenReturn(Optional.of(statusId));
        when(movementTypeRepository.findIdByName(MovementTypeConstants.TRANSFER_OUT)).thenReturn(Optional.of(UUID.randomUUID()));
        when(movementTypeRepository.findIdByName(MovementTypeConstants.TRANSFER_IN)).thenReturn(Optional.of(UUID.randomUUID()));

        StockBalance sourceBal = StockBalance.builder()
                .id(UUID.randomUUID())
                .warehouse(StockBalance.WarehouseRef.builder().id(fromWh).build())
                .location(StockBalance.LocationRef.builder().id(fromLoc).build())
                .product(StockBalance.ProductRef.builder().id(prodId).build())
                .lot(StockBalance.LotRef.builder().id(tLotId).build())
                .stockStatus(StockBalance.StockStatusRef.builder().id(statusId).build())
                .quantity(new BigDecimal("20.00"))
                .build();

        when(balanceRepository.findForUpdate(fromWh, fromLoc, prodId, tLotId, statusId))
                .thenReturn(Optional.of(sourceBal));

        assertThatThrownBy(() -> inventoryService.transferStock(request))
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
        UUID tLotId = UUID.randomUUID();
        UUID statusId = UUID.randomUUID();

        StockTransferRequest request = StockTransferRequest.builder()
                .fromWarehouseId(fromWh)
                .fromLocationId(fromLoc)
                .toWarehouseId(toWh)
                .toLocationId(toLoc)
                .productId(prodId)
                .lotId(tLotId)
                .quantity(new BigDecimal("20.00"))
                .build();

        when(warehouseLocationQueryPort.belongsToWarehouse(fromLoc, fromWh)).thenReturn(true);
        when(warehouseLocationQueryPort.belongsToWarehouse(toLoc, toWh)).thenReturn(true);
        when(stockStatusRepository.findIdByName(StockStatusConstants.AVAILABLE)).thenReturn(Optional.of(statusId));
        when(movementTypeRepository.findIdByName(MovementTypeConstants.TRANSFER_OUT)).thenReturn(Optional.of(UUID.randomUUID()));
        when(movementTypeRepository.findIdByName(MovementTypeConstants.TRANSFER_IN)).thenReturn(Optional.of(UUID.randomUUID()));
        when(currentUserPort.getCurrentUserId()).thenReturn(userId);

        StockBalance sourceBal = StockBalance.builder()
                .id(UUID.randomUUID())
                .warehouse(StockBalance.WarehouseRef.builder().id(fromWh).build())
                .location(StockBalance.LocationRef.builder().id(fromLoc).build())
                .product(StockBalance.ProductRef.builder().id(prodId).build())
                .lot(StockBalance.LotRef.builder().id(tLotId).build())
                .stockStatus(StockBalance.StockStatusRef.builder().id(statusId).build())
                .quantity(new BigDecimal("50.00"))
                .build();

        StockBalance destBal = StockBalance.builder()
                .id(UUID.randomUUID())
                .warehouse(StockBalance.WarehouseRef.builder().id(toWh).build())
                .location(StockBalance.LocationRef.builder().id(toLoc).build())
                .product(StockBalance.ProductRef.builder().id(prodId).build())
                .lot(StockBalance.LotRef.builder().id(tLotId).build())
                .stockStatus(StockBalance.StockStatusRef.builder().id(statusId).build())
                .quantity(new BigDecimal("10.00"))
                .build();

        when(balanceRepository.findForUpdate(fromWh, fromLoc, prodId, tLotId, statusId)).thenReturn(Optional.of(sourceBal));
        when(balanceRepository.findForUpdate(toWh, toLoc, prodId, tLotId, statusId)).thenReturn(Optional.of(destBal));
        when(balanceRepository.save(any(StockBalance.class))).thenAnswer(i -> i.getArgument(0));
        when(movementRepository.save(any(StockMovement.class))).thenAnswer(i -> i.getArgument(0));

        fpt.qn.mes.inventory.application.dto.response.StockTransferResponse response = inventoryService.transferStock(request);

        assertThat(response).isNotNull();
        assertThat(sourceBal.getQuantity()).isEqualByComparingTo(new BigDecimal("30.00"));
        assertThat(destBal.getQuantity()).isEqualByComparingTo(new BigDecimal("30.00"));
        verify(movementRepository, org.mockito.Mockito.times(2)).save(any(StockMovement.class));
    }
}
