package fpt.qn.mes.inventory.presentation;

import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@RestController
@RequestMapping("/api/stock-lots")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class StockLotController {

    StockLotUseCase stockLotUseCase;

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<StockLotResponse>>> getStockLots(
            @ModelAttribute StockLotSearchRequest request) {
        return ResponseEntity.ok(ApiResponse.success(stockLotUseCase.getStockLots(request), "OK"));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<StockLotResponse>> getStockLotById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(stockLotUseCase.getStockLotById(id), "OK"));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Void>> createStockLot(
            @Valid @RequestBody CreateStockLotRequest request) {
        stockLotUseCase.createStockLot(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Created"));
    }
}
