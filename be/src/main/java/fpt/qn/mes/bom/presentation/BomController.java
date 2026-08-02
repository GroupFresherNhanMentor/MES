package fpt.qn.mes.bom.presentation;

import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import fpt.qn.mes.bom.application.dto.bom.BomResponse;
import fpt.qn.mes.bom.application.dto.bom.create.CreateBomRequest;
import fpt.qn.mes.bom.application.dto.bom.search.BomSearchRequest;
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
    public ResponseEntity<ApiResponse<PageResponse<BomResponse>>> getBoms(@ModelAttribute BomSearchRequest request) {
        return ResponseEntity.ok(ApiResponse.success(bomUseCase.getBoms(request), "OK"));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'PLANNER', 'FACTORY_MANAGER', 'OPERATOR', 'QC_INSPECTOR')")
    public ResponseEntity<ApiResponse<BomResponse>> getBomById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(bomUseCase.getBomById(id), "OK"));
    }

    @PostMapping
    @PreAuthorize("hasRole('PLANNER')")
    public ResponseEntity<ApiResponse<Void>> createBom(@Valid @RequestBody CreateBomRequest request) {
        bomUseCase.createBom(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Created"));
    }

    @PostMapping("/{id}/activate")
    @PreAuthorize("hasRole('PLANNER')")
    public ResponseEntity<ApiResponse<Void>> activateBom(@PathVariable UUID id) {
        bomUseCase.activateBom(id);
        return ResponseEntity.ok(ApiResponse.success("Activated"));
    }

    @PostMapping("/{id}/deactivate")
    @PreAuthorize("hasRole('PLANNER')")
    public ResponseEntity<ApiResponse<Void>> deactivateBom(@PathVariable UUID id) {
        bomUseCase.deactivateBom(id);
        return ResponseEntity.ok(ApiResponse.success("Deactivated"));
    }

    @PostMapping("/{id}/new-version")
    @PreAuthorize("hasRole('PLANNER')")
    public ResponseEntity<ApiResponse<BomResponse>> createNewVersion(@PathVariable UUID id) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(bomUseCase.createNewVersion(id), "Created"));
    }
}
