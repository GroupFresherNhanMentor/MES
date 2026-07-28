package fpt.qn.mes.quality.presentation;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
import fpt.qn.mes.quality.application.dto.request.CreateDefectTypeRequest;
import fpt.qn.mes.quality.application.dto.request.CreateLookupRequest;
import fpt.qn.mes.quality.application.dto.request.CreateQualityInspectionRequest;
import fpt.qn.mes.quality.application.dto.request.FailQcRequest;
import fpt.qn.mes.quality.application.dto.request.PassQcRequest;
import fpt.qn.mes.quality.application.dto.response.DefectTypeDto;
import fpt.qn.mes.quality.application.dto.response.FailQcResponse;
import fpt.qn.mes.quality.application.dto.response.PassQcResponse;
import fpt.qn.mes.quality.application.dto.response.QcActionDto;
import fpt.qn.mes.quality.application.dto.response.QcStatusDto;
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
    public ResponseEntity<ApiResponse<PageResponse<QualityInspectionDto>>> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(
            ApiResponse.success(qualityUseCase.getInspections(page, size), "OK"));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<QualityInspectionDto>> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(
            ApiResponse.success(qualityUseCase.getInspectionById(id), "OK"));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<QualityInspectionDto>> create(
            @Valid @RequestBody CreateQualityInspectionRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.success(qualityUseCase.createInspection(req), "Created"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        qualityUseCase.deleteInspection(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Deleted"));
    }

    @PostMapping("/{inspectionId}/pass")
    public ResponseEntity<ApiResponse<PassQcResponse>> pass(
            @PathVariable UUID inspectionId,
            @Valid @RequestBody PassQcRequest request) {
        return ResponseEntity.ok(
            ApiResponse.success(qualityUseCase.passInspection(inspectionId, request), "Pass QC thành công"));
    }

    @PostMapping("/{inspectionId}/fail")
    public ResponseEntity<ApiResponse<FailQcResponse>> fail(
            @PathVariable UUID inspectionId,
            @Valid @RequestBody FailQcRequest request) {
        return ResponseEntity.ok(
            ApiResponse.success(qualityUseCase.failInspection(inspectionId, request), "Fail QC thành công"));
    }

    @GetMapping("/statuses")
    public ResponseEntity<ApiResponse<List<QcStatusDto>>> getQcStatuses() {
        return ResponseEntity.ok(
            ApiResponse.success(qualityUseCase.getQcStatuses(), "OK"));
    }

    @PostMapping("/statuses")
    public ResponseEntity<ApiResponse<QcStatusDto>> createQcStatus(
            @Valid @RequestBody CreateLookupRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.success(qualityUseCase.createQcStatus(request), "Created"));
    }

    @GetMapping("/actions")
    public ResponseEntity<ApiResponse<List<QcActionDto>>> getQcActions() {
        return ResponseEntity.ok(
            ApiResponse.success(qualityUseCase.getQcActions(), "OK"));
    }

    @PostMapping("/actions")
    public ResponseEntity<ApiResponse<QcActionDto>> createQcAction(
            @Valid @RequestBody CreateLookupRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.success(qualityUseCase.createQcAction(request), "Created"));
    }

    @GetMapping("/defect-types")
    public ResponseEntity<ApiResponse<List<DefectTypeDto>>> getDefectTypes() {
        return ResponseEntity.ok(
            ApiResponse.success(qualityUseCase.getDefectTypes(), "OK"));
    }

    @PostMapping("/defect-types")
    public ResponseEntity<ApiResponse<DefectTypeDto>> createDefectType(
            @Valid @RequestBody CreateDefectTypeRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.success(qualityUseCase.createDefectType(request), "Created"));
    }
}
