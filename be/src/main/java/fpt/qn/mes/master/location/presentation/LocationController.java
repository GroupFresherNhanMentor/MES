package fpt.qn.mes.master.location.presentation;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import fpt.qn.mes.common.dto.ApiResponse;
import fpt.qn.mes.master.location.application.dto.CreateLocationRequest;
import fpt.qn.mes.master.location.application.dto.UpdateLocationRequest;
import fpt.qn.mes.master.location.application.dto.WarehouseLocationDto;
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
    public ResponseEntity<ApiResponse<List<WarehouseLocationDto>>> getLocations(@PathVariable UUID warehouseId) {
        throw new UnsupportedOperationException("Not implemented");
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<WarehouseLocationDto>> getLocationById(@PathVariable UUID warehouseId, @PathVariable UUID id) {
        throw new UnsupportedOperationException("Not implemented");
    }

    @PostMapping
    public ResponseEntity<ApiResponse<WarehouseLocationDto>> createLocation(
            @PathVariable UUID warehouseId, @Valid @RequestBody CreateLocationRequest request, @AuthenticationPrincipal Jwt jwt) {
        throw new UnsupportedOperationException("Not implemented");
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<WarehouseLocationDto>> updateLocation(
            @PathVariable UUID warehouseId, @PathVariable UUID id, @RequestBody UpdateLocationRequest request, @AuthenticationPrincipal Jwt jwt) {
        throw new UnsupportedOperationException("Not implemented");
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteLocation(@PathVariable UUID warehouseId, @PathVariable UUID id) {
        throw new UnsupportedOperationException("Not implemented");
    }

    @GetMapping("/statuses")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getLocationStatuses() {
        throw new UnsupportedOperationException("Not implemented");
    }
}
