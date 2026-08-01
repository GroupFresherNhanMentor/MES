package fpt.qn.mes.inventory.presentation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import fpt.qn.mes.common.dto.response.ApiResponse;
import fpt.qn.mes.common.dto.response.PageResponse;
import fpt.qn.mes.inventory.application.dto.response.StockTransferResponse;
import fpt.qn.mes.inventory.application.dto.stockadjustment.create.CreateStockAdjustmentRequest;
import fpt.qn.mes.inventory.application.dto.stockadjustmentapproval.StockAdjustmentApprovalResponse;
import fpt.qn.mes.inventory.application.dto.stockbalance.StockBalanceResponse;
import fpt.qn.mes.inventory.application.dto.stockbalance.search.StockBalanceSearchRequest;
import fpt.qn.mes.inventory.application.dto.stockmovement.StockMovementResponse;
import fpt.qn.mes.inventory.application.dto.stockmovement.create.StockInRequest;
import fpt.qn.mes.inventory.application.dto.stockmovement.create.StockTransferRequest;
import fpt.qn.mes.inventory.application.dto.stockmovement.search.StockMovementSearchRequest;
import fpt.qn.mes.inventory.application.port.in.InventoryUseCase;
import fpt.qn.mes.inventory.application.dto.stockadjustmentapproval.search.StockAdjustmentApprovalSearchRequest;

@ExtendWith(MockitoExtension.class)
class InventoryControllerTest {

    @Mock
    InventoryUseCase inventoryUseCase;

    @InjectMocks
    InventoryController controller;

    UUID productId;
    UUID warehouseId;
    UUID locationId;

    @BeforeEach
    void setUp() {
        productId = UUID.randomUUID();
        warehouseId = UUID.randomUUID();
        locationId = UUID.randomUUID();
    }

    @Test
    @DisplayName("getMovements should return 200 OK with PageResponse of StockMovementResponse")
    void getMovements_shouldReturn200OK() {
        StockMovementSearchRequest request = new StockMovementSearchRequest();
        request.setPage(0);
        request.setSize(20);
        request.setReferenceNo("PO-2026-001");

        StockMovementResponse dto = StockMovementResponse.builder()
                .id(UUID.randomUUID())
                .referenceNo("PO-2026-001")
                .quantity(new BigDecimal("100.00"))
                .createdAt(Instant.now())
                .build();

        PageResponse<StockMovementResponse> pageResponse = PageResponse.<StockMovementResponse>builder()
                .items(List.of(dto))
                .totalElements(1)
                .totalPages(1)
                .pageNumber(0)
                .pageSize(20)
                .build();

        when(inventoryUseCase.getMovements(any(StockMovementSearchRequest.class))).thenReturn(pageResponse);

        ResponseEntity<ApiResponse<PageResponse<StockMovementResponse>>> response = controller.getMovements(request);

        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().isSuccess()).isTrue();
        assertThat(response.getBody().getData().getItems()).hasSize(1);
        assertThat(response.getBody().getData().getItems().get(0).getReferenceNo()).isEqualTo("PO-2026-001");
        verify(inventoryUseCase).getMovements(any(StockMovementSearchRequest.class));
    }

    @Test
    @DisplayName("recordStockIn should return 201 Created")
    void recordStockIn_shouldReturn201Created() {
        StockInRequest request = StockInRequest.builder()
                .productId(productId)
                .warehouseId(warehouseId)
                .locationId(locationId)
                .lotNumber("LOT-IN-001")
                .quantity(new BigDecimal("100.00"))
                .referenceNo("PO-STOCK-IN")
                .build();

        ResponseEntity<ApiResponse<Void>> response = controller.recordStockIn(request);

        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        verify(inventoryUseCase).recordStockIn(any(StockInRequest.class));
    }

    @Test
    @DisplayName("getStockBalances should return 200 OK")
    void getStockBalances_shouldReturn200OK() {
        StockBalanceSearchRequest request = new StockBalanceSearchRequest();
        StockBalanceResponse dto = StockBalanceResponse.builder()
                .id(UUID.randomUUID())
                .quantity(new BigDecimal("10.00"))
                .build();
        PageResponse<StockBalanceResponse> pageResponse = PageResponse.of(List.of(dto), 1, 0, 20);

        when(inventoryUseCase.getStockBalances(any(StockBalanceSearchRequest.class))).thenReturn(pageResponse);

        ResponseEntity<ApiResponse<PageResponse<StockBalanceResponse>>> response = controller.getStockBalances(request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getData().getItems()).hasSize(1);
    }

    @Test
    @DisplayName("adjustStock returns 200 OK")
    void adjustStock_returns200OK() {
        CreateStockAdjustmentRequest request = CreateStockAdjustmentRequest.builder()
                .stockBalanceId(UUID.randomUUID())
                .quantityAdjustment(new BigDecimal("10.00"))
                .reason("Count fix")
                .build();

        ResponseEntity<ApiResponse<Void>> response = controller.adjustStock(request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(inventoryUseCase).adjustStock(any(CreateStockAdjustmentRequest.class));
    }

    @Test
    @DisplayName("getPendingAdjustments returns 200 OK with list")
    void getPendingAdjustments_returns200OK() {
        StockAdjustmentApprovalSearchRequest criteria = new StockAdjustmentApprovalSearchRequest();
        criteria.setPage(0);
        criteria.setSize(20);

        StockAdjustmentApprovalResponse dto = StockAdjustmentApprovalResponse.builder()
                .id(UUID.randomUUID()).build();
        PageResponse<StockAdjustmentApprovalResponse> pageResponse = PageResponse.of(List.of(dto), 1, 0, 20);

        when(inventoryUseCase.getPendingAdjustments(any())).thenReturn(pageResponse);

        ResponseEntity<ApiResponse<PageResponse<StockAdjustmentApprovalResponse>>> response =
                controller.getPendingAdjustments(criteria);


        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getData().getItems()).hasSize(1);
    }

    @Test
    @DisplayName("approveAdjustment returns 200 OK with movement response")
    void approveAdjustment_returns200OK() {
        UUID approvalId = UUID.randomUUID();
        StockMovementResponse movementDto = StockMovementResponse.builder().id(UUID.randomUUID()).build();

        when(inventoryUseCase.approveAdjustment(eq(approvalId))).thenReturn(movementDto);

        ResponseEntity<ApiResponse<StockMovementResponse>> response = controller.approveAdjustment(approvalId);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getData()).isNotNull();
    }

    @Test
    @DisplayName("rejectAdjustment returns 200 OK")
    void rejectAdjustment_returns200OK() {
        UUID approvalId = UUID.randomUUID();

        ResponseEntity<ApiResponse<Void>> response = controller.rejectAdjustment(approvalId);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(inventoryUseCase).rejectAdjustment(eq(approvalId));
    }

    @Test
    @DisplayName("transferStock should return 200 OK with StockTransferResponse")
    void transferStock_shouldReturn200OK() {
        StockTransferRequest request = StockTransferRequest.builder()
                .fromWarehouseId(UUID.randomUUID())
                .fromLocationId(UUID.randomUUID())
                .toWarehouseId(UUID.randomUUID())
                .toLocationId(UUID.randomUUID())
                .productId(UUID.randomUUID())
                .lotId(UUID.randomUUID())
                .quantity(new BigDecimal("20.00"))
                .build();

        StockTransferResponse serviceResponse = StockTransferResponse.builder()
                .transferOutMovement(StockMovementResponse.builder().id(UUID.randomUUID()).build())
                .transferInMovement(StockMovementResponse.builder().id(UUID.randomUUID()).build())
                .sourceBalance(StockBalanceResponse.builder().quantity(new BigDecimal("30.00")).build())
                .destinationBalance(StockBalanceResponse.builder().quantity(new BigDecimal("20.00")).build())
                .build();

        when(inventoryUseCase.transferStock(any(StockTransferRequest.class))).thenReturn(serviceResponse);

        ResponseEntity<ApiResponse<StockTransferResponse>> response = controller.transferStock(request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getData()).isNotNull();
        assertThat(response.getBody().getData().getSourceBalance().getQuantity())
                .isEqualByComparingTo(new BigDecimal("30.00"));
    }
}
