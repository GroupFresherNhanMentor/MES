package fpt.qn.mes.quality.presentation;

import java.util.List;
import java.util.Map;
import java.util.UUID;

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

import fpt.qn.mes.common.dto.response.ApiResponse;
import fpt.qn.mes.common.dto.response.PageResponse;
import fpt.qn.mes.quality.application.dto.request.CreateInspectionResultRequest;
import fpt.qn.mes.quality.application.dto.request.CreateQualityInspectionRequest;
import fpt.qn.mes.quality.application.dto.response.QualityInspectionDto;
import fpt.qn.mes.quality.application.dto.response.QualityInspectionResultDto;
import fpt.qn.mes.quality.application.port.in.QualityUseCase;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@RestController
@RequestMapping("/api/quality-inspections")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class QualityController {

    QualityUseCase qualityUseCase;

    @GetMapping
    @PreAuthorize("hasAuthority('QUALITY_INSPECTION_READ')")
    public ResponseEntity<ApiResponse<PageResponse<QualityInspectionDto>>> getAll(
            @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size) {
        throw new UnsupportedOperationException("Not implemented");
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('QUALITY_INSPECTION_READ')")
    public ResponseEntity<ApiResponse<QualityInspectionDto>> getById(@PathVariable UUID id) {
        throw new UnsupportedOperationException("Not implemented");
    }

    @PostMapping
    @PreAuthorize("hasAuthority('QUALITY_INSPECTION_CREATE')")
    public ResponseEntity<ApiResponse<QualityInspectionDto>> create(
            @Valid @RequestBody CreateQualityInspectionRequest req) {
        throw new UnsupportedOperationException("Not implemented");
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('QUALITY_INSPECTION_DELETE')")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        throw new UnsupportedOperationException("Not implemented");
    }

    @GetMapping("/{inspectionId}/results")
    @PreAuthorize("hasAuthority('QUALITY_RESULT_READ')")
    public ResponseEntity<ApiResponse<PageResponse<QualityInspectionResultDto>>> getResults(
            @PathVariable UUID inspectionId,
            @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size) {
        throw new UnsupportedOperationException("Not implemented");
    }

    @PostMapping("/{inspectionId}/results")
    @PreAuthorize("hasAuthority('QUALITY_RESULT_CREATE')")
    public ResponseEntity<ApiResponse<QualityInspectionResultDto>> addResult(
            @PathVariable UUID inspectionId, @Valid @RequestBody CreateInspectionResultRequest req) {
        throw new UnsupportedOperationException("Not implemented");
    }

    @GetMapping("/statuses")
    @PreAuthorize("hasAuthority('LOOKUP_READ')")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getQcStatuses() {
        throw new UnsupportedOperationException("Not implemented");
    }

    @GetMapping("/defect-types")
    @PreAuthorize("hasAuthority('LOOKUP_READ')")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getDefectTypes() {
        throw new UnsupportedOperationException("Not implemented");
    }
}
