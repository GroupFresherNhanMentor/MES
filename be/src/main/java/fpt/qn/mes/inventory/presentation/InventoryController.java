package fpt.qn.mes.inventory.presentation;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
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

@Tag(name = "Inventory", description = "Stock balances, lots, and movement management APIs")
@RestController
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class InventoryController {

    InventoryUseCase inventoryUseCase;

    @GetMapping("/api/stock-lots")
    @PreAuthorize("hasAuthority('STOCK_LOT_READ')")
    public ResponseEntity<ApiResponse<PageResponse<StockLotDto>>> getStockLots(
            @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size) {
        throw new UnsupportedOperationException("Not implemented");
    }

    @GetMapping("/api/stock-lots/{id}")
    @PreAuthorize("hasAuthority('STOCK_LOT_READ')")
    public ResponseEntity<ApiResponse<StockLotDto>> getStockLotById(@PathVariable UUID id) {
        throw new UnsupportedOperationException("Not implemented");
    }

    @PostMapping("/api/stock-lots")
    @PreAuthorize("hasAuthority('STOCK_LOT_CREATE')")
    public ResponseEntity<ApiResponse<StockLotDto>> createStockLot(@Valid @RequestBody CreateStockLotRequest request) {
        throw new UnsupportedOperationException("Not implemented");
    }

    @GetMapping("/api/stock-movements")
    @PreAuthorize("hasAuthority('STOCK_MOVEMENT_READ')")
    public ResponseEntity<ApiResponse<PageResponse<StockMovementDto>>> getMovements(
            @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size) {
        throw new UnsupportedOperationException("Not implemented");
    }

    @PostMapping("/api/stock-movements")
    @PreAuthorize("hasAuthority('STOCK_MOVEMENT_CREATE')")
    public ResponseEntity<ApiResponse<StockMovementDto>> recordMovement(
            @Valid @RequestBody CreateMovementRequest request, @AuthenticationPrincipal Jwt jwt) {
        throw new UnsupportedOperationException("Not implemented");
    }

    @GetMapping("/api/stock-balances")
    @PreAuthorize("hasAuthority('STOCK_BALANCE_READ')")
    public ResponseEntity<ApiResponse<PageResponse<StockBalanceDto>>> getStockBalances(
            @Valid StockBalanceSearchRequest request) {
        PageResponse<StockBalanceDto> result = inventoryUseCase.getStockBalances(request);
        return ResponseEntity.ok(ApiResponse.success(result, "OK"));
    }

    @GetMapping("/api/lot-types")
    @PreAuthorize("hasAuthority('LOOKUP_READ')")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getLotTypes() {
        throw new UnsupportedOperationException("Not implemented");
    }

    @GetMapping("/api/stock-statuses")
    @PreAuthorize("hasAuthority('LOOKUP_READ')")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getStockStatuses() {
        throw new UnsupportedOperationException("Not implemented");
    }

    @GetMapping("/api/movement-types")
    @PreAuthorize("hasAuthority('LOOKUP_READ')")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getMovementTypes() {
        throw new UnsupportedOperationException("Not implemented");
    }
}
