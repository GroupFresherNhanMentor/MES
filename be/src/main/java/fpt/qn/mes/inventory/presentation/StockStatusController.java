package fpt.qn.mes.inventory.presentation;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import fpt.qn.mes.common.dto.response.ApiResponse;
import fpt.qn.mes.common.dto.response.PageResponse;
import fpt.qn.mes.inventory.application.dto.stockstatus.StockStatusResponse;
import fpt.qn.mes.inventory.application.dto.stockstatus.create.CreateStockStatusRequest;
import fpt.qn.mes.inventory.application.dto.stockstatus.search.StockStatusSearchRequest;
import fpt.qn.mes.inventory.application.port.in.StockStatusUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@Tag(name = "Stock Statuses", description = "Stock status lookup management APIs")
@RestController
@RequestMapping("/api/stock-statuses")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class StockStatusController {

    StockStatusUseCase stockStatusUseCase;

    @Operation(summary = "Get stock statuses")
    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<StockStatusResponse>>> getStockStatuses(
            @ModelAttribute StockStatusSearchRequest request) {
        return ResponseEntity.ok(ApiResponse.success(stockStatusUseCase.getStockStatuses(request), "OK"));
    }

    @Operation(summary = "Create stock status")
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> createStockStatus(
            @Valid @RequestBody CreateStockStatusRequest request) {
        stockStatusUseCase.createStockStatus(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Created"));
    }
}
