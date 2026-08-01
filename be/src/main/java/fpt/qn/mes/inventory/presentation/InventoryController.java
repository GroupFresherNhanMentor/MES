package fpt.qn.mes.inventory.presentation;

import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import fpt.qn.mes.common.dto.response.ApiResponse;
import fpt.qn.mes.common.dto.response.PageResponse;
import fpt.qn.mes.inventory.application.dto.response.StockTransferResponse;
import fpt.qn.mes.inventory.application.port.in.InventoryUseCase;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

import org.springframework.http.HttpStatus;
import io.swagger.v3.oas.annotations.Operation;

import fpt.qn.mes.inventory.application.dto.stockadjustmentapproval.StockAdjustmentApprovalResponse;
import fpt.qn.mes.inventory.application.dto.stockadjustmentapproval.search.StockAdjustmentApprovalSearchRequest;
import fpt.qn.mes.inventory.application.dto.stockadjustment.create.CreateStockAdjustmentRequest;
import fpt.qn.mes.inventory.application.dto.stockmovement.create.StockInRequest;
import fpt.qn.mes.inventory.application.dto.stockmovement.create.StockTransferRequest;
import fpt.qn.mes.inventory.application.dto.stockmovement.search.StockMovementSearchRequest;
import fpt.qn.mes.inventory.application.dto.stockbalance.StockBalanceResponse;
import fpt.qn.mes.inventory.application.dto.stockmovement.StockMovementResponse;
import fpt.qn.mes.inventory.application.dto.stockbalance.search.StockBalanceSearchRequest;


@Tag(name = "Inventory", description = "Stock balances, lots, and movement management APIs")
@RestController
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class InventoryController {

    InventoryUseCase inventoryUseCase;

    @Operation(summary = "Search stock movements with pagination and criteria filtering")
    @GetMapping("/api/stock-movements")
    public ResponseEntity<ApiResponse<PageResponse<StockMovementResponse>>> getMovements(
            @ModelAttribute @Valid StockMovementSearchRequest request) {
        PageResponse<StockMovementResponse> result = inventoryUseCase.getMovements(request);
        return ResponseEntity.ok(ApiResponse.success(result, "OK"));
    }

    @Operation(summary = "Record incoming stock physical receipt into warehouse")
    @PostMapping("/api/stock-in")
    public ResponseEntity<ApiResponse<Void>> recordStockIn(
            @Valid @RequestBody StockInRequest request) {
        inventoryUseCase.recordStockIn(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Stock received successfully"));
    }

    @GetMapping("/api/stock-balances")
    public ResponseEntity<ApiResponse<PageResponse<StockBalanceResponse>>> getStockBalances(
            @ModelAttribute @Valid StockBalanceSearchRequest request) {
        PageResponse<StockBalanceResponse> result = inventoryUseCase.getStockBalances(request);
        return ResponseEntity.ok(ApiResponse.success(result, "OK"));
    }

    @Operation(summary = "Submit a stock adjustment request")
    @PostMapping("/api/stock-adjustments")
    public ResponseEntity<ApiResponse<Void>> adjustStock(
            @Valid @RequestBody CreateStockAdjustmentRequest request) {
        inventoryUseCase.adjustStock(request);
        return ResponseEntity.ok(ApiResponse.success("Stock adjustment processed"));
    }

    @Operation(summary = "Get pending stock adjustments requiring approval (Factory Manager)")
    @GetMapping("/api/stock-adjustments/pending")
    // @PreAuthorize("hasRole('FACTORY_MANAGER')")
    public ResponseEntity<ApiResponse<PageResponse<StockAdjustmentApprovalResponse>>> getPendingAdjustments(
            @ModelAttribute @Valid StockAdjustmentApprovalSearchRequest request) {
        PageResponse<StockAdjustmentApprovalResponse> result = inventoryUseCase.getPendingAdjustments(request);
        return ResponseEntity.ok(ApiResponse.success(result, "OK"));
    }

    @Operation(summary = "Approve a pending stock adjustment (Factory Manager)")
    @PostMapping("/api/stock-adjustments/{id}/approve")
    // @PreAuthorize("hasRole('FACTORY_MANAGER')")
    public ResponseEntity<ApiResponse<StockMovementResponse>> approveAdjustment(
            @PathVariable UUID id) {
        StockMovementResponse result = inventoryUseCase.approveAdjustment(id);
        return ResponseEntity.ok(ApiResponse.success(result, "Adjustment approved and balance updated successfully"));
    }

    @Operation(summary = "Reject a pending stock adjustment (Factory Manager)")
    @PostMapping("/api/stock-adjustments/{id}/reject")
    // @PreAuthorize("hasRole('FACTORY_MANAGER')")
    public ResponseEntity<ApiResponse<Void>> rejectAdjustment(
            @PathVariable UUID id) {
        inventoryUseCase.rejectAdjustment(id);
        return ResponseEntity.ok(ApiResponse.success("Adjustment rejected and request removed"));
    }

    @Operation(summary = "Transfer available inventory between warehouse locations")
    @PostMapping("/api/stock-transfers")
    public ResponseEntity<ApiResponse<StockTransferResponse>> transferStock(
            @Valid @RequestBody StockTransferRequest request) {
            StockTransferResponse result = inventoryUseCase.transferStock(request);
        return ResponseEntity.ok(ApiResponse.success(result, "Stock transfer completed successfully"));
    }
}
