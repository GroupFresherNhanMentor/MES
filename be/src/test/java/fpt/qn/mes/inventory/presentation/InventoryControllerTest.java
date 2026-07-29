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

import fpt.qn.mes.auth.application.security.AppUserPrincipal;
import fpt.qn.mes.common.dto.response.ApiResponse;
import fpt.qn.mes.common.dto.response.PageResponse;
import fpt.qn.mes.inventory.application.dto.request.CreateMovementRequest;
import fpt.qn.mes.inventory.application.dto.request.CreateStockLotRequest;
import fpt.qn.mes.inventory.application.dto.request.StockBalanceSearchRequest;
import fpt.qn.mes.inventory.application.dto.request.StockInRequest;
import fpt.qn.mes.inventory.application.dto.request.StockLotSearchRequest;
import fpt.qn.mes.inventory.application.dto.request.StockMovementSearchRequest;
import fpt.qn.mes.inventory.application.dto.response.StockBalanceDto;
import fpt.qn.mes.inventory.application.dto.response.StockLotDto;
import fpt.qn.mes.inventory.application.dto.response.StockMovementDto;
import fpt.qn.mes.inventory.application.port.in.InventoryUseCase;

@ExtendWith(MockitoExtension.class)
class InventoryControllerTest {

    @Mock
    InventoryUseCase inventoryUseCase;

    @InjectMocks
    InventoryController controller;

    UUID productId;
    UUID warehouseId;
    UUID locationId;
    UUID userId;

    @BeforeEach
    void setUp() {
        productId = UUID.randomUUID();
        warehouseId = UUID.randomUUID();
        locationId = UUID.randomUUID();
        userId = UUID.randomUUID();
    }

    @Test
    @DisplayName("getMovements should return 200 OK with PageResponse of StockMovementDto")
    void getMovements_shouldReturn200OK() {
        StockMovementSearchRequest request = new StockMovementSearchRequest();
        request.setPage(0);
        request.setSize(20);
        request.setReferenceNo("PO-2026-001");

        StockMovementDto dto = StockMovementDto.builder()
                .id(UUID.randomUUID())
                .referenceNo("PO-2026-001")
                .quantity(new BigDecimal("100.00"))
                .createdAt(Instant.now())
                .build();

        PageResponse<StockMovementDto> pageResponse = PageResponse.<StockMovementDto>builder()
                .items(List.of(dto))
                .totalElements(1)
                .totalPages(1)
                .pageNumber(0)
                .pageSize(20)
                .build();

        when(inventoryUseCase.getMovements(any(StockMovementSearchRequest.class))).thenReturn(pageResponse);

        ResponseEntity<ApiResponse<PageResponse<StockMovementDto>>> response = controller.getMovements(request);

        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().isSuccess()).isTrue();
        assertThat(response.getBody().getData().getItems()).hasSize(1);
        assertThat(response.getBody().getData().getItems().get(0).getReferenceNo()).isEqualTo("PO-2026-001");

        verify(inventoryUseCase).getMovements(any(StockMovementSearchRequest.class));
    }

    @Test
    @DisplayName("recordMovement should return 201 Created")
    void recordMovement_shouldReturn201Created() {
        CreateMovementRequest request = new CreateMovementRequest();
        request.setProductId(productId);
        request.setWarehouseId(warehouseId);
        request.setQuantity(new BigDecimal("50.00"));

        StockMovementDto dto = StockMovementDto.builder()
                .id(UUID.randomUUID())
                .quantity(new BigDecimal("50.00"))
                .build();

        AppUserPrincipal principal = AppUserPrincipal.builder().id(userId).username("user@mes.com").enabled(true).roles(List.of("ROLE_USER")).build();
        when(inventoryUseCase.recordMovement(any(CreateMovementRequest.class), eq(userId))).thenReturn(dto);

        ResponseEntity<ApiResponse<StockMovementDto>> response = controller.recordMovement(request, principal);

        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getData().getQuantity()).isEqualTo(new BigDecimal("50.00"));

        verify(inventoryUseCase).recordMovement(any(CreateMovementRequest.class), eq(userId));
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

        StockMovementDto dto = StockMovementDto.builder()
                .id(UUID.randomUUID())
                .referenceNo("PO-STOCK-IN")
                .quantity(new BigDecimal("100.00"))
                .build();

        AppUserPrincipal principal = AppUserPrincipal.builder().id(userId).username("user@mes.com").enabled(true).roles(List.of("ROLE_USER")).build();
        when(inventoryUseCase.recordStockIn(any(StockInRequest.class), eq(userId))).thenReturn(dto);

        ResponseEntity<ApiResponse<StockMovementDto>> response = controller.recordStockIn(request, principal);

        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody().getData().getReferenceNo()).isEqualTo("PO-STOCK-IN");

        verify(inventoryUseCase).recordStockIn(any(StockInRequest.class), eq(userId));
    }

    @Test
    @DisplayName("getStockLots should return 200 OK")
    void getStockLots_shouldReturn200OK() {
        StockLotSearchRequest request = new StockLotSearchRequest();
        StockLotDto dto = StockLotDto.builder().id(UUID.randomUUID()).lotNumber("LOT-001").build();
        PageResponse<StockLotDto> pageResponse = PageResponse.of(List.of(dto), 1, 0, 20);

        when(inventoryUseCase.getStockLots(any(StockLotSearchRequest.class))).thenReturn(pageResponse);

        ResponseEntity<ApiResponse<PageResponse<StockLotDto>>> response = controller.getStockLots(request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getData().getItems()).hasSize(1);
    }

    @Test
    @DisplayName("getStockBalances should return 200 OK")
    void getStockBalances_shouldReturn200OK() {
        StockBalanceSearchRequest request = new StockBalanceSearchRequest();
        StockBalanceDto dto = StockBalanceDto.builder().id(UUID.randomUUID()).quantity(new BigDecimal("10.00")).build();
        PageResponse<StockBalanceDto> pageResponse = PageResponse.of(List.of(dto), 1, 0, 20);

        when(inventoryUseCase.getStockBalances(any(StockBalanceSearchRequest.class))).thenReturn(pageResponse);

        ResponseEntity<ApiResponse<PageResponse<StockBalanceDto>>> response = controller.getStockBalances(request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getData().getItems()).hasSize(1);
    }
}
