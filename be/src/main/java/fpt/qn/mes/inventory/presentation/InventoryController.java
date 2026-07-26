package fpt.qn.mes.inventory.presentation;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import fpt.qn.mes.common.dto.ApiResponse;
import fpt.qn.mes.common.dto.PageResponse;
import fpt.qn.mes.inventory.application.dto.CreateMovementRequest;
import fpt.qn.mes.inventory.application.dto.CreateStockLotRequest;
import fpt.qn.mes.inventory.application.dto.StockBalanceDto;
import fpt.qn.mes.inventory.application.dto.StockLotDto;
import fpt.qn.mes.inventory.application.dto.StockMovementDto;
import fpt.qn.mes.inventory.application.port.in.InventoryUseCase;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@RestController
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class InventoryController {

    InventoryUseCase inventoryUseCase;

    @GetMapping("/api/stock-lots")
    public ResponseEntity<ApiResponse<PageResponse<StockLotDto>>> getStockLots(
            @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size) {
        throw new UnsupportedOperationException("Not implemented");
    }

    @GetMapping("/api/stock-lots/{id}")
    public ResponseEntity<ApiResponse<StockLotDto>> getStockLotById(@PathVariable UUID id) {
        throw new UnsupportedOperationException("Not implemented");
    }

    @PostMapping("/api/stock-lots")
    public ResponseEntity<ApiResponse<StockLotDto>> createStockLot(@Valid @RequestBody CreateStockLotRequest request) {
        throw new UnsupportedOperationException("Not implemented");
    }

    @GetMapping("/api/stock-movements")
    public ResponseEntity<ApiResponse<PageResponse<StockMovementDto>>> getMovements(
            @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size) {
        throw new UnsupportedOperationException("Not implemented");
    }

    @PostMapping("/api/stock-movements")
    public ResponseEntity<ApiResponse<StockMovementDto>> recordMovement(
            @Valid @RequestBody CreateMovementRequest request, @AuthenticationPrincipal Jwt jwt) {
        throw new UnsupportedOperationException("Not implemented");
    }

    @GetMapping("/api/stock-balances")
    public ResponseEntity<ApiResponse<List<StockBalanceDto>>> getStockBalances(
            @RequestParam(required = false) UUID warehouseId,
            @RequestParam(required = false) UUID productId) {
        throw new UnsupportedOperationException("Not implemented");
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
