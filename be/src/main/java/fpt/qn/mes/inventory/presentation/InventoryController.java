package fpt.qn.mes.inventory.presentation;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import fpt.qn.mes.common.dto.response.ApiResponse;
import fpt.qn.mes.common.dto.response.PageResponse;
import fpt.qn.mes.inventory.application.dto.request.CreateMovementRequest;
import fpt.qn.mes.inventory.application.dto.request.CreateStockLotRequest;
import fpt.qn.mes.inventory.application.dto.request.StockBalanceSearchRequest;
import fpt.qn.mes.inventory.application.dto.response.StockBalanceDto;
import fpt.qn.mes.inventory.application.dto.response.StockLotDto;
import fpt.qn.mes.inventory.application.dto.response.StockMovementDto;
import fpt.qn.mes.inventory.application.port.in.InventoryUseCase;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

import org.springframework.http.HttpStatus;
import fpt.qn.mes.inventory.application.dto.request.StockInRequest;
import fpt.qn.mes.inventory.application.dto.request.StockLotSearchRequest;
import io.swagger.v3.oas.annotations.Operation;

import fpt.qn.mes.auth.application.security.AppUserPrincipal;

@Tag(name = "Inventory", description = "Stock balances, lots, and movement management APIs")
@RestController
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class InventoryController {

    private static final UUID DEFAULT_USER_ID = UUID.fromString("019facbf-316d-71ee-8fa1-78cda592c099");

    InventoryUseCase inventoryUseCase;

    @Operation(summary = "Search stock lots with pagination and criteria filtering")
    @GetMapping("/api/stock-lots")
    public ResponseEntity<ApiResponse<PageResponse<StockLotDto>>> getStockLots(@Valid StockLotSearchRequest request) {
        PageResponse<StockLotDto> result = inventoryUseCase.getStockLots(request);
        return ResponseEntity.ok(ApiResponse.success(result, "OK"));
    }

    @GetMapping("/api/stock-lots/{id}")
    public ResponseEntity<ApiResponse<StockLotDto>> getStockLotById(@PathVariable UUID id) {
        StockLotDto result = inventoryUseCase.getStockLotById(id);
        return ResponseEntity.ok(ApiResponse.success(result, "OK"));
    }

    @PostMapping("/api/stock-lots")
    public ResponseEntity<ApiResponse<StockLotDto>> createStockLot(@Valid @RequestBody CreateStockLotRequest request) {
        StockLotDto result = inventoryUseCase.createStockLot(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(result, "Stock lot created successfully"));
    }

    @Operation(summary = "Search stock movements with pagination and criteria filtering")
    @GetMapping("/api/stock-movements")
    public ResponseEntity<ApiResponse<PageResponse<StockMovementDto>>> getMovements(
            @Valid fpt.qn.mes.inventory.application.dto.request.StockMovementSearchRequest request) {
        PageResponse<StockMovementDto> result = inventoryUseCase.getMovements(request);
        return ResponseEntity.ok(ApiResponse.success(result, "OK"));
    }

    @PostMapping("/api/stock-movements")
    public ResponseEntity<ApiResponse<StockMovementDto>> recordMovement(
            @Valid @RequestBody CreateMovementRequest request, @AuthenticationPrincipal AppUserPrincipal principal) {
        UUID currentUserId = principal != null ? principal.getId() : DEFAULT_USER_ID;
        StockMovementDto result = inventoryUseCase.recordMovement(request, currentUserId);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(result, "Movement recorded successfully"));
    }

    @Operation(summary = "Record incoming stock physical receipt into warehouse")
    @PostMapping("/api/stock-in")
    public ResponseEntity<ApiResponse<StockMovementDto>> recordStockIn(
            @Valid @RequestBody StockInRequest request, @AuthenticationPrincipal AppUserPrincipal principal) {
        UUID currentUserId = principal != null ? principal.getId() : DEFAULT_USER_ID;
        StockMovementDto result = inventoryUseCase.recordStockIn(request, currentUserId);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(result, "Stock received successfully"));
    }

    @GetMapping("/api/stock-balances")
    public ResponseEntity<ApiResponse<PageResponse<StockBalanceDto>>> getStockBalances(
            @Valid StockBalanceSearchRequest request) {
        PageResponse<StockBalanceDto> result = inventoryUseCase.getStockBalances(request);
        return ResponseEntity.ok(ApiResponse.success(result, "OK"));
    }

    @GetMapping("/api/lot-types")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getLotTypes() {
        throw new UnsupportedOperationException("Not implemented");
    }

    @GetMapping("/api/stock-statuses")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getStockStatuses() {
        throw new UnsupportedOperationException("Not implemented");
    }

    @GetMapping("/api/movement-types")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getMovementTypes() {
        throw new UnsupportedOperationException("Not implemented");
    }
}
