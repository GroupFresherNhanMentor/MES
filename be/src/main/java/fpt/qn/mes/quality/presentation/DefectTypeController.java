package fpt.qn.mes.quality.presentation;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import fpt.qn.mes.common.dto.response.ApiResponse;
import fpt.qn.mes.common.dto.response.PageResponse;
import fpt.qn.mes.quality.application.dto.defecttype.DefectTypeResponse;
import fpt.qn.mes.quality.application.dto.defecttype.create.CreateDefectTypeRequest;
import fpt.qn.mes.quality.application.dto.defecttype.search.DefectTypeSearchRequest;
import fpt.qn.mes.quality.application.port.in.DefectTypeUseCase;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@RestController
@RequestMapping("/api/defect-types")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class DefectTypeController {

    DefectTypeUseCase defectTypeUseCase;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<PageResponse<DefectTypeResponse>>> getDefectTypes(
            @ModelAttribute DefectTypeSearchRequest request) {
        return ResponseEntity.ok(ApiResponse.success(defectTypeUseCase.getDefectTypes(request), "OK"));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> createDefectType(
            @Valid @RequestBody CreateDefectTypeRequest request) {
        defectTypeUseCase.createDefectType(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Created"));
    }
}
