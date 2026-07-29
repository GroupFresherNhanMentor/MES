package fpt.qn.mes.master.warehouse.presentation;

import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import fpt.qn.mes.auth.application.security.AppUserPrincipal;
import fpt.qn.mes.common.dto.response.ApiResponse;
import fpt.qn.mes.common.dto.response.PageResponse;
import fpt.qn.mes.master.warehouse.application.dto.request.CreateWarehouseRequest;
import fpt.qn.mes.master.warehouse.application.dto.request.UpdateWarehouseRequest;
import fpt.qn.mes.master.warehouse.application.dto.response.WarehouseDto;
import fpt.qn.mes.master.warehouse.application.port.in.WarehouseUseCase;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@RestController
@RequestMapping("/api/warehouses")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class WarehouseController {

    WarehouseUseCase warehouseUseCase;

    @GetMapping
    @PreAuthorize("hasAuthority('WAREHOUSE_READ')")
    public ResponseEntity<ApiResponse<PageResponse<WarehouseDto>>> getWarehouses(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) UUID statusId) {
        var result = (statusId != null)
                ? warehouseUseCase.getWarehousesByStatus(page, size, statusId)
                : warehouseUseCase.getWarehouses(page, size);
        return ResponseEntity.ok(ApiResponse.success(result, "OK"));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('WAREHOUSE_READ')")
    public ResponseEntity<ApiResponse<WarehouseDto>> getWarehouseById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(warehouseUseCase.getWarehouseById(id), "OK"));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('WAREHOUSE_CREATE')")
    public ResponseEntity<ApiResponse<WarehouseDto>> createWarehouse(
            @Valid @RequestBody CreateWarehouseRequest request,
            @AuthenticationPrincipal AppUserPrincipal principal) {
        var result = warehouseUseCase.createWarehouse(request, principal.getId());
        return ResponseEntity.status(201).body(ApiResponse.success(result, "Created"));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('WAREHOUSE_UPDATE')")
    public ResponseEntity<ApiResponse<WarehouseDto>> updateWarehouse(
            @PathVariable UUID id, @RequestBody UpdateWarehouseRequest request,
            @AuthenticationPrincipal AppUserPrincipal principal) {
        var result = warehouseUseCase.updateWarehouse(id, request, principal.getId());
        return ResponseEntity.ok(ApiResponse.success(result, "Updated"));
    }

    @PutMapping("/{id}/deactivate")
    @PreAuthorize("hasAuthority('WAREHOUSE_DEACTIVATE')")
    public ResponseEntity<ApiResponse<Void>> deactivateWarehouse(@PathVariable UUID id) {
        warehouseUseCase.deleteWarehouse(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Deactivated"));
    }
}
