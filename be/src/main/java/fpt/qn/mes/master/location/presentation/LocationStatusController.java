package fpt.qn.mes.master.location.presentation;

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
import fpt.qn.mes.master.location.application.dto.locationstatus.LocationStatusResponse;
import fpt.qn.mes.master.location.application.dto.locationstatus.create.CreateLocationStatusRequest;
import fpt.qn.mes.master.location.application.dto.locationstatus.search.LocationStatusSearchRequest;
import fpt.qn.mes.master.location.application.port.in.LocationStatusUseCase;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@RestController
@RequestMapping("/api/location-statuses")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class LocationStatusController {

    LocationStatusUseCase locationstatusUseCase;

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<LocationStatusResponse>>> getLocationStatuses(
            @ModelAttribute LocationStatusSearchRequest request) {
        return ResponseEntity.ok(ApiResponse.success(locationstatusUseCase.getLocationStatuses(request), "OK"));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Void>> createLocationStatus(
            @Valid @RequestBody CreateLocationStatusRequest request) {
        locationstatusUseCase.createLocationStatus(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Created"));
    }
}
