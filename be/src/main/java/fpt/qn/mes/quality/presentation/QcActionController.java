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
import fpt.qn.mes.quality.application.dto.qcaction.QcActionResponse;
import fpt.qn.mes.quality.application.dto.qcaction.create.CreateQcActionRequest;
import fpt.qn.mes.quality.application.dto.qcaction.search.QcActionSearchRequest;
import fpt.qn.mes.quality.application.port.in.QcActionUseCase;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@RestController
@RequestMapping("/api/qc-actions")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class QcActionController {

    QcActionUseCase qcActionUseCase;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<PageResponse<QcActionResponse>>> getQcActions(
            @ModelAttribute QcActionSearchRequest request) {
        return ResponseEntity.ok(ApiResponse.success(qcActionUseCase.getQcActions(request), "OK"));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> createQcAction(
            @Valid @RequestBody CreateQcActionRequest request) {
        qcActionUseCase.createQcAction(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Created"));
    }
}
