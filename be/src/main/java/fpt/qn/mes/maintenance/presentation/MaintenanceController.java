package fpt.qn.mes.maintenance.presentation;

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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import fpt.qn.mes.common.dto.response.ApiResponse;
import fpt.qn.mes.common.dto.response.PageResponse;
import fpt.qn.mes.maintenance.application.dto.request.CreateDowntimeRequest;
import fpt.qn.mes.maintenance.application.dto.request.CreateMaintenanceTicketRequest;
import fpt.qn.mes.maintenance.application.dto.response.MachineDowntimeDto;
import fpt.qn.mes.maintenance.application.dto.response.MaintenanceTicketDto;
import fpt.qn.mes.maintenance.application.dto.request.UpdateMaintenanceTicketRequest;
import fpt.qn.mes.maintenance.application.port.in.MaintenanceUseCase;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@RestController
@RequestMapping("/api/maintenance-tickets")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class MaintenanceController {

    MaintenanceUseCase maintenanceUseCase;

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<MaintenanceTicketDto>>> getAll(
            @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size) {
        throw new UnsupportedOperationException("Not implemented");
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<MaintenanceTicketDto>> getById(@PathVariable UUID id) {
        throw new UnsupportedOperationException("Not implemented");
    }

    @PostMapping
    public ResponseEntity<ApiResponse<MaintenanceTicketDto>> create(
            @Valid @RequestBody CreateMaintenanceTicketRequest req, @AuthenticationPrincipal Jwt jwt) {
        throw new UnsupportedOperationException("Not implemented");
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<MaintenanceTicketDto>> update(
            @PathVariable UUID id, @RequestBody UpdateMaintenanceTicketRequest req) {
        throw new UnsupportedOperationException("Not implemented");
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        throw new UnsupportedOperationException("Not implemented");
    }

    @GetMapping("/{ticketId}/downtime")
    public ResponseEntity<ApiResponse<PageResponse<MachineDowntimeDto>>> getDowntimes(
            @PathVariable UUID ticketId,
            @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size) {
        throw new UnsupportedOperationException("Not implemented");
    }

    @PostMapping("/{ticketId}/downtime")
    public ResponseEntity<ApiResponse<MachineDowntimeDto>> addDowntime(
            @PathVariable UUID ticketId, @Valid @RequestBody CreateDowntimeRequest req) {
        throw new UnsupportedOperationException("Not implemented");
    }

    @GetMapping("/types")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getTicketTypes() {
        throw new UnsupportedOperationException("Not implemented");
    }

    @GetMapping("/statuses")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getTicketStatuses() {
        throw new UnsupportedOperationException("Not implemented");
    }

    @GetMapping("/priorities")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getTicketPriorities() {
        throw new UnsupportedOperationException("Not implemented");
    }
}
