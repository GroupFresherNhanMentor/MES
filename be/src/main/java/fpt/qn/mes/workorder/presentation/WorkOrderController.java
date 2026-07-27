package fpt.qn.mes.workorder.presentation;

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
import fpt.qn.mes.workorder.application.dto.request.CreateWorkOrderEventRequest;
import fpt.qn.mes.workorder.application.dto.request.CreateWorkOrderMaterialRequest;
import fpt.qn.mes.workorder.application.dto.request.CreateWorkOrderRequest;
import fpt.qn.mes.workorder.application.dto.request.UpdateWorkOrderRequest;
import fpt.qn.mes.workorder.application.dto.response.WorkOrderDto;
import fpt.qn.mes.workorder.application.dto.response.WorkOrderEventDto;
import fpt.qn.mes.workorder.application.dto.response.WorkOrderMaterialDto;
import fpt.qn.mes.workorder.application.port.in.WorkOrderUseCase;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@RestController
@RequestMapping("/api/work-orders")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class WorkOrderController {

    WorkOrderUseCase workOrderUseCase;

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<WorkOrderDto>>> getAll(
            @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size) {
        throw new UnsupportedOperationException("Not implemented");
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<WorkOrderDto>> getById(@PathVariable UUID id) {
        throw new UnsupportedOperationException("Not implemented");
    }

    @PostMapping
    public ResponseEntity<ApiResponse<WorkOrderDto>> create(
            @Valid @RequestBody CreateWorkOrderRequest req, @AuthenticationPrincipal Jwt jwt) {
        throw new UnsupportedOperationException("Not implemented");
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<WorkOrderDto>> update(
            @PathVariable UUID id, @RequestBody UpdateWorkOrderRequest req) {
        throw new UnsupportedOperationException("Not implemented");
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        throw new UnsupportedOperationException("Not implemented");
    }

    @GetMapping("/{workOrderId}/materials")
    public ResponseEntity<ApiResponse<PageResponse<WorkOrderMaterialDto>>> getMaterials(
            @PathVariable UUID workOrderId,
            @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size) {
        throw new UnsupportedOperationException("Not implemented");
    }

    @PostMapping("/{workOrderId}/materials")
    public ResponseEntity<ApiResponse<WorkOrderMaterialDto>> addMaterial(
            @PathVariable UUID workOrderId, @Valid @RequestBody CreateWorkOrderMaterialRequest req) {
        throw new UnsupportedOperationException("Not implemented");
    }

    @DeleteMapping("/{workOrderId}/materials/{materialId}")
    public ResponseEntity<ApiResponse<Void>> deleteMaterial(
            @PathVariable UUID workOrderId, @PathVariable UUID materialId) {
        throw new UnsupportedOperationException("Not implemented");
    }

    @GetMapping("/{workOrderId}/events")
    public ResponseEntity<ApiResponse<PageResponse<WorkOrderEventDto>>> getEvents(
            @PathVariable UUID workOrderId,
            @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size) {
        throw new UnsupportedOperationException("Not implemented");
    }

    @PostMapping("/{workOrderId}/events")
    public ResponseEntity<ApiResponse<WorkOrderEventDto>> addEvent(
            @PathVariable UUID workOrderId, @Valid @RequestBody CreateWorkOrderEventRequest req) {
        throw new UnsupportedOperationException("Not implemented");
    }

    @GetMapping("/statuses")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getWorkOrderStatuses() {
        throw new UnsupportedOperationException("Not implemented");
    }

    @GetMapping("/priorities")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getWorkOrderPriorities() {
        throw new UnsupportedOperationException("Not implemented");
    }

    @GetMapping("/event-types")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getWorkOrderEventTypes() {
        throw new UnsupportedOperationException("Not implemented");
    }
}
