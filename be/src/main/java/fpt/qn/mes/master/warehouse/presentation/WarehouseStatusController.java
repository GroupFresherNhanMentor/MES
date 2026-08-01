package fpt.qn.mes.master.warehouse.presentation;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import fpt.qn.mes.common.dto.response.ApiResponse;
import fpt.qn.mes.common.dto.response.PageResponse;
import fpt.qn.mes.master.warehouse.application.dto.warehousestatus.WarehouseStatusResponse;
import fpt.qn.mes.master.warehouse.application.dto.warehousestatus.create.CreateWarehouseStatusRequest;
import fpt.qn.mes.master.warehouse.application.dto.warehousestatus.search.WarehouseStatusSearchRequest;
import fpt.qn.mes.master.warehouse.application.port.in.WarehouseStatusUseCase;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@RestController
@RequestMapping("/api/warehouse-statuses")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class WarehouseStatusController {

    WarehouseStatusUseCase warehousestatusUseCase;

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<WarehouseStatusResponse>>> getWarehouseStatuses(
            @ModelAttribute WarehouseStatusSearchRequest request) {
        return ResponseEntity.ok(ApiResponse.success(warehousestatusUseCase.getWarehouseStatuses(request), "OK"));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Void>> createWarehouseStatus(
            @Valid @RequestBody CreateWarehouseStatusRequest request) {
        warehousestatusUseCase.createWarehouseStatus(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Created"));
    }
}
