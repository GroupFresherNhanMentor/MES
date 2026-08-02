package fpt.qn.mes.master.location.presentation;

import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
import fpt.qn.mes.master.location.application.dto.warehouselocation.WarehouseLocationResponse;
import fpt.qn.mes.master.location.application.dto.warehouselocation.create.CreateWarehouseLocationRequest;
import fpt.qn.mes.master.location.application.dto.warehouselocation.search.WarehouseLocationSearchRequest;
import fpt.qn.mes.master.location.application.dto.warehouselocation.update.UpdateWarehouseLocationRequest;
import fpt.qn.mes.master.location.application.port.in.WarehouseLocationUseCase;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@RestController
@RequestMapping("/api/warehouses/{warehouseId}/locations")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class WarehouseLocationController {

    WarehouseLocationUseCase warehouseLocationUseCase;

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<WarehouseLocationResponse>>> getWarehouseLocations(
            @PathVariable UUID warehouseId, @ModelAttribute WarehouseLocationSearchRequest request) {
        return ResponseEntity.ok(ApiResponse.success(warehouseLocationUseCase.getWarehouseLocations(warehouseId, request), "OK"));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<WarehouseLocationResponse>> getWarehouseLocationById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(warehouseLocationUseCase.getWarehouseLocationById(id), "OK"));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Void>> createWarehouseLocation(
            @PathVariable UUID warehouseId, @Valid @RequestBody CreateWarehouseLocationRequest request) {
        warehouseLocationUseCase.createWarehouseLocation(warehouseId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Created"));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> updateWarehouseLocation(
            @PathVariable UUID id, @Valid @RequestBody UpdateWarehouseLocationRequest request) {
        warehouseLocationUseCase.updateWarehouseLocation(id, request);
        return ResponseEntity.ok(ApiResponse.success("Updated"));
    }

    @PutMapping("/{id}/activate")
    public ResponseEntity<ApiResponse<Void>> activateWarehouseLocation(@PathVariable UUID id) {
        warehouseLocationUseCase.activateWarehouseLocation(id);
        return ResponseEntity.ok(ApiResponse.success("Activated"));
    }

    @PutMapping("/{id}/deactivate")
    public ResponseEntity<ApiResponse<Void>> deactivateWarehouseLocation(@PathVariable UUID id) {
        warehouseLocationUseCase.deactivateWarehouseLocation(id);
        return ResponseEntity.ok(ApiResponse.success("Deactivated"));
    }
}
