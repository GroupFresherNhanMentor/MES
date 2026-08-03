package fpt.qn.mes.master.line.presentation;

import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
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
import fpt.qn.mes.master.line.application.dto.line.LineResponse;
import fpt.qn.mes.master.line.application.dto.line.create.CreateLineRequest;
import fpt.qn.mes.master.line.application.dto.line.search.LineSearchRequest;
import fpt.qn.mes.master.line.application.dto.line.update.UpdateLineRequest;
import fpt.qn.mes.master.line.application.port.in.LineUseCase;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@RestController
@RequestMapping("/api/lines")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class LineController {

    LineUseCase lineUseCase;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERATOR', 'PLANNER', 'FACTORY_MANAGER', 'AUDITOR', 'MAINTENANCE_ENGINEER')")
    public ResponseEntity<ApiResponse<PageResponse<LineResponse>>> getLines(
            @ModelAttribute LineSearchRequest request) {
        return ResponseEntity.ok(ApiResponse.success(lineUseCase.getLines(request), "OK"));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERATOR', 'PLANNER', 'FACTORY_MANAGER', 'AUDITOR', 'MAINTENANCE_ENGINEER')")
    public ResponseEntity<ApiResponse<LineResponse>> getLineById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(lineUseCase.getLineById(id), "OK"));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> createLine(
            @Valid @RequestBody CreateLineRequest request) {
        lineUseCase.createLine(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Created"));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> updateLine(
            @PathVariable UUID id, @Valid @RequestBody UpdateLineRequest request) {
        lineUseCase.updateLine(id, request);
        return ResponseEntity.ok(ApiResponse.success("Updated"));
    }

    @PutMapping("/{id}/activate")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> activateLine(@PathVariable UUID id) {
        lineUseCase.activateLine(id);
        return ResponseEntity.ok(ApiResponse.success("Activated"));
    }

    @PutMapping("/{id}/deactivate")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deactivateLine(@PathVariable UUID id) {
        lineUseCase.deactivateLine(id);
        return ResponseEntity.ok(ApiResponse.success("Deactivated"));
    }
}
