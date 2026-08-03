package fpt.qn.mes.master.warehouse.presentation;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import fpt.qn.mes.common.dto.response.ApiResponse;
import fpt.qn.mes.common.dto.response.PageResponse;
import fpt.qn.mes.master.warehouse.application.dto.warehouse.WarehouseManagerResponse;
import fpt.qn.mes.master.warehouse.application.dto.warehouse.WarehouseResponse;
import fpt.qn.mes.master.warehouse.application.dto.warehouse.assign.AssignManagerRequest;
import fpt.qn.mes.master.warehouse.application.dto.warehouse.create.CreateWarehouseRequest;
import fpt.qn.mes.master.warehouse.application.dto.warehouse.search.WarehouseSearchRequest;
import fpt.qn.mes.master.warehouse.application.dto.warehouse.update.UpdateWarehouseRequest;
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
    @PreAuthorize("hasAnyRole('ADMIN', 'WAREHOUSE_MANAGER', 'PLANNER', 'FACTORY_MANAGER', 'AUDITOR', 'OPERATOR')")
    public ResponseEntity<ApiResponse<PageResponse<WarehouseResponse>>> getWarehouses(
            @ModelAttribute WarehouseSearchRequest request) {
        return ResponseEntity.ok(ApiResponse.success(warehouseUseCase.getWarehouses(request), "OK"));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'WAREHOUSE_MANAGER', 'PLANNER', 'FACTORY_MANAGER', 'AUDITOR', 'OPERATOR')")
    public ResponseEntity<ApiResponse<WarehouseResponse>> getWarehouseById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(warehouseUseCase.getWarehouseById(id), "OK"));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> createWarehouse(
            @Valid @RequestBody CreateWarehouseRequest request) {
        warehouseUseCase.createWarehouse(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Created"));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> updateWarehouse(
            @PathVariable UUID id, @Valid @RequestBody UpdateWarehouseRequest request) {
        warehouseUseCase.updateWarehouse(id, request);
        return ResponseEntity.ok(ApiResponse.success("Updated"));
    }

    @PutMapping("/{id}/activate")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> activateWarehouse(@PathVariable UUID id) {
        warehouseUseCase.activateWarehouse(id);
        return ResponseEntity.ok(ApiResponse.success("Activated"));
    }

    @PutMapping("/{id}/deactivate")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deactivateWarehouse(@PathVariable UUID id) {
        warehouseUseCase.deactivateWarehouse(id);
        return ResponseEntity.ok(ApiResponse.success("Deactivated"));
    }

    @GetMapping("/{id}/managers")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<WarehouseManagerResponse>>> getManagers(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(warehouseUseCase.getManagers(id), "OK"));
    }

    @PostMapping("/{id}/managers")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> assignManager(
            @PathVariable UUID id, @Valid @RequestBody AssignManagerRequest request) {
        warehouseUseCase.assignManager(id, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Manager assigned"));
    }

    @DeleteMapping("/{id}/managers/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> removeManager(
            @PathVariable UUID id, @PathVariable UUID userId) {
        warehouseUseCase.removeManager(id, userId);
        return ResponseEntity.ok(ApiResponse.success("Manager removed"));
    }
}
