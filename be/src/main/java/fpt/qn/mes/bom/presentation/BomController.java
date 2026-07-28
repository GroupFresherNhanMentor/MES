package fpt.qn.mes.bom.presentation;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import fpt.qn.mes.bom.application.dto.request.CreateBomItemRequest;
import fpt.qn.mes.bom.application.dto.request.CreateBomRequest;
import fpt.qn.mes.bom.application.dto.response.BomDto;
import fpt.qn.mes.bom.application.dto.response.BomItemDto;
import fpt.qn.mes.bom.application.port.in.BomUseCase;
import fpt.qn.mes.common.dto.response.ApiResponse;
import fpt.qn.mes.common.dto.response.PageResponse;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@RestController
@RequestMapping("/api/boms")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class BomController {

    BomUseCase bomUseCase;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'PLANNER', 'FACTORY_MANAGER', 'OPERATOR', 'QC_INSPECTOR')")
    public ResponseEntity<ApiResponse<PageResponse<BomDto>>> getBoms(
            @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(ApiResponse.success(bomUseCase.getBoms(page, size), "OK"));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'PLANNER', 'FACTORY_MANAGER', 'OPERATOR', 'QC_INSPECTOR')")
    public ResponseEntity<ApiResponse<BomDto>> getBomById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(bomUseCase.getBomById(id), "OK"));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'PLANNER')")
    public ResponseEntity<ApiResponse<BomDto>> createBom(
            @Valid @RequestBody CreateBomRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(bomUseCase.createBom(request), "Created"));
    }

    @PostMapping("/{id}/activate")
    @PreAuthorize("hasAnyRole('ADMIN', 'PLANNER')")
    public ResponseEntity<ApiResponse<BomDto>> activateBom(@PathVariable UUID id) {
        throw new UnsupportedOperationException("Not implemented");
    }

    @PostMapping("/{bomId}/items")
    @PreAuthorize("hasAnyRole('ADMIN', 'PLANNER')")
    public ResponseEntity<ApiResponse<BomItemDto>> addBomItem(
            @PathVariable UUID bomId, @Valid @RequestBody CreateBomItemRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(bomUseCase.addBomItem(bomId, request), "Created"));
    }

    @DeleteMapping("/{bomId}/items/{itemId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'PLANNER')")
    public ResponseEntity<ApiResponse<Void>> deleteBomItem(
            @PathVariable UUID bomId, @PathVariable UUID itemId) {
        bomUseCase.deleteBomItem(bomId, itemId);
        return ResponseEntity.ok(ApiResponse.success(null, "Deleted"));
    }

    @GetMapping("/statuses")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getBomStatuses() {
        throw new UnsupportedOperationException("Not implemented");
    }
}
