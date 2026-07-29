package fpt.qn.mes.master.location.presentation;

import java.util.List;
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
import org.springframework.web.bind.annotation.RestController;

import fpt.qn.mes.auth.application.security.AppUserPrincipal;
import fpt.qn.mes.common.dto.response.ApiResponse;
import fpt.qn.mes.master.location.application.dto.request.CreateLocationRequest;
import fpt.qn.mes.master.location.application.dto.request.UpdateLocationRequest;
import fpt.qn.mes.master.location.application.dto.response.WarehouseLocationDto;
import fpt.qn.mes.master.location.application.port.in.LocationUseCase;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@RestController
@RequestMapping("/api/warehouses/{warehouseId}/locations")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class LocationController {

    LocationUseCase locationUseCase;

    @GetMapping
    @PreAuthorize("hasAuthority('LOCATION_READ')")
    public ResponseEntity<ApiResponse<List<WarehouseLocationDto>>> getLocations(@PathVariable UUID warehouseId) {
        return ResponseEntity.ok(ApiResponse.success(locationUseCase.getLocations(warehouseId), "OK"));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('LOCATION_READ')")
    public ResponseEntity<ApiResponse<WarehouseLocationDto>> getLocationById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(locationUseCase.getLocationById(id), "OK"));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('LOCATION_CREATE')")
    public ResponseEntity<ApiResponse<WarehouseLocationDto>> createLocation(
            @PathVariable UUID warehouseId, @Valid @RequestBody CreateLocationRequest request,
            @AuthenticationPrincipal AppUserPrincipal principal) {
        var result = locationUseCase.createLocation(warehouseId, request, principal.getId());
        return ResponseEntity.status(201).body(ApiResponse.success(result, "Created"));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('LOCATION_UPDATE')")
    public ResponseEntity<ApiResponse<WarehouseLocationDto>> updateLocation(
            @PathVariable UUID id, @RequestBody UpdateLocationRequest request,
            @AuthenticationPrincipal AppUserPrincipal principal) {
        var result = locationUseCase.updateLocation(id, request, principal.getId());
        return ResponseEntity.ok(ApiResponse.success(result, "Updated"));
    }

    @PutMapping("/{id}/deactivate")
    @PreAuthorize("hasAuthority('LOCATION_DEACTIVATE')")
    public ResponseEntity<ApiResponse<Void>> deactivateLocation(@PathVariable UUID id) {
        locationUseCase.deleteLocation(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Deactivated"));
    }
}
