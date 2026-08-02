package fpt.qn.mes.inventory.presentation;

import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import fpt.qn.mes.common.dto.response.ApiResponse;
import fpt.qn.mes.common.dto.response.PageResponse;
import fpt.qn.mes.inventory.application.dto.stocklot.StockLotResponse;
import fpt.qn.mes.inventory.application.dto.stocklot.create.CreateStockLotRequest;
import fpt.qn.mes.inventory.application.dto.stocklot.search.StockLotSearchRequest;
import fpt.qn.mes.inventory.application.port.in.StockLotUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@Tag(name = "Stock Lots", description = "Stock lot management APIs")
@RestController
@RequestMapping("/api/stock-lots")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class StockLotController {

    StockLotUseCase stockLotUseCase;

    @Operation(summary = "Search stock lots with pagination")
    @GetMapping
    @PreAuthorize("hasAnyRole('WAREHOUSE_MANAGER', 'FACTORY_MANAGER', 'ADMIN', 'OPERATOR', 'PLANNER', 'QC_INSPECTOR')")
    public ResponseEntity<ApiResponse<PageResponse<StockLotResponse>>> getStockLots(
            @ModelAttribute StockLotSearchRequest request) {
        return ResponseEntity.ok(ApiResponse.success(stockLotUseCase.getStockLots(request), "OK"));
    }

    @Operation(summary = "Get stock lot by ID")
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('WAREHOUSE_MANAGER', 'FACTORY_MANAGER', 'ADMIN', 'OPERATOR', 'PLANNER', 'QC_INSPECTOR')")
    public ResponseEntity<ApiResponse<StockLotResponse>> getStockLotById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(stockLotUseCase.getStockLotById(id), "OK"));
    }

    @Operation(summary = "Create stock lot")
    @PostMapping
    @PreAuthorize("hasAnyRole('WAREHOUSE_MANAGER', 'FACTORY_MANAGER', 'ADMIN')")
    public ResponseEntity<ApiResponse<Void>> createStockLot(
            @Valid @RequestBody CreateStockLotRequest request) {
        stockLotUseCase.createStockLot(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Created"));
    }
}
