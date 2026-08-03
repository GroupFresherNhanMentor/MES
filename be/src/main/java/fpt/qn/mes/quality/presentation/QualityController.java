package fpt.qn.mes.quality.presentation;

import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import fpt.qn.mes.common.dto.response.ApiResponse;
import fpt.qn.mes.common.dto.response.PageResponse;
import fpt.qn.mes.quality.application.dto.inspection.QualityInspectionResponse;
import fpt.qn.mes.quality.application.dto.inspection.QualityInspectionResultResponse;
import fpt.qn.mes.quality.application.dto.inspection.create.CreateQualityInspectionRequest;
import fpt.qn.mes.quality.application.dto.inspection.fail.FailQcRequest;
import fpt.qn.mes.quality.application.dto.inspection.pass.PassQcRequest;
import fpt.qn.mes.quality.application.dto.inspection.result.search.QualityInspectionResultSearchRequest;
import fpt.qn.mes.quality.application.dto.inspection.search.QualityInspectionSearchRequest;
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
    @PreAuthorize("hasAnyRole('ADMIN', 'QC_INSPECTOR', 'FACTORY_MANAGER', 'AUDITOR', 'OPERATOR', 'PLANNER')")
    public ResponseEntity<ApiResponse<PageResponse<QualityInspectionResponse>>> getAll(
            @Valid QualityInspectionSearchRequest request) {
        return ResponseEntity.ok(ApiResponse.success(qualityUseCase.getInspections(request), "OK"));
    }

    @GetMapping("/{id}/results")
    @PreAuthorize("hasAnyRole('ADMIN', 'QC_INSPECTOR', 'FACTORY_MANAGER', 'AUDITOR', 'OPERATOR', 'PLANNER')")
    public ResponseEntity<ApiResponse<PageResponse<QualityInspectionResultResponse>>> getResults(
            @PathVariable UUID id,
            @Valid QualityInspectionResultSearchRequest request) {
        return ResponseEntity.ok(ApiResponse.success(qualityUseCase.getInspectionResults(id, request), "OK"));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'QC_INSPECTOR')")
    public ResponseEntity<ApiResponse<Void>> create(
            @Valid @RequestBody CreateQualityInspectionRequest req) {
        qualityUseCase.createInspection(req);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Created"));
    }

    @PostMapping("/{inspectionId}/pass")
    @PreAuthorize("hasAnyRole('ADMIN', 'QC_INSPECTOR')")
    public ResponseEntity<ApiResponse<Void>> pass(
            @PathVariable UUID inspectionId,
            @Valid @RequestBody PassQcRequest request) {
        qualityUseCase.passInspection(inspectionId, request);
        return ResponseEntity.ok(ApiResponse.success("Pass QC successful"));
    }

    @PostMapping("/{inspectionId}/fail")
    @PreAuthorize("hasAnyRole('ADMIN', 'QC_INSPECTOR')")
    public ResponseEntity<ApiResponse<Void>> fail(
            @PathVariable UUID inspectionId,
            @Valid @RequestBody FailQcRequest request) {
        qualityUseCase.failInspection(inspectionId, request);
        return ResponseEntity.ok(ApiResponse.success("Fail QC successful"));
    }
}
