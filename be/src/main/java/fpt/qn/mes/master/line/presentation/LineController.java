package fpt.qn.mes.master.line.presentation;

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
import fpt.qn.mes.master.line.application.dto.request.CreateLineRequest;
import fpt.qn.mes.master.line.application.dto.request.UpdateLineRequest;
import fpt.qn.mes.master.line.application.dto.response.ProductionLineDto;
import fpt.qn.mes.master.line.application.port.in.LineUseCase;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@RestController
@RequestMapping("/api/production-lines")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class LineController {

    LineUseCase lineUseCase;

    @GetMapping
    @PreAuthorize("hasAuthority('PRODUCTION_LINE_READ')")
    public ResponseEntity<ApiResponse<PageResponse<ProductionLineDto>>> getLines(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) UUID statusId) {
        var result = (statusId != null)
                ? lineUseCase.getLinesByStatus(page, size, statusId)
                : lineUseCase.getLines(page, size);
        return ResponseEntity.ok(ApiResponse.success(result, "OK"));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('PRODUCTION_LINE_READ')")
    public ResponseEntity<ApiResponse<ProductionLineDto>> getLineById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(lineUseCase.getLineById(id), "OK"));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('PRODUCTION_LINE_CREATE')")
    public ResponseEntity<ApiResponse<ProductionLineDto>> createLine(
            @Valid @RequestBody CreateLineRequest request,
            @AuthenticationPrincipal AppUserPrincipal principal) {
        var result = lineUseCase.createLine(request, principal.getId());
        return ResponseEntity.status(201).body(ApiResponse.success(result, "Created"));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('PRODUCTION_LINE_UPDATE')")
    public ResponseEntity<ApiResponse<ProductionLineDto>> updateLine(
            @PathVariable UUID id, @RequestBody UpdateLineRequest request,
            @AuthenticationPrincipal AppUserPrincipal principal) {
        var result = lineUseCase.updateLine(id, request, principal.getId());
        return ResponseEntity.ok(ApiResponse.success(result, "Updated"));
    }

    @PutMapping("/{id}/deactivate")
    @PreAuthorize("hasAuthority('PRODUCTION_LINE_DEACTIVATE')")
    public ResponseEntity<ApiResponse<Void>> deactivateLine(@PathVariable UUID id) {
        lineUseCase.deleteLine(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Deactivated"));
    }
}
