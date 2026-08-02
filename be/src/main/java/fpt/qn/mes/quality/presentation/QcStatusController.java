package fpt.qn.mes.quality.presentation;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import fpt.qn.mes.common.dto.response.ApiResponse;
import fpt.qn.mes.common.dto.response.PageResponse;
import fpt.qn.mes.quality.application.dto.qcstatus.QcStatusResponse;
import fpt.qn.mes.quality.application.dto.qcstatus.create.CreateQcStatusRequest;
import fpt.qn.mes.quality.application.dto.qcstatus.search.QcStatusSearchRequest;
import fpt.qn.mes.quality.application.port.in.QcStatusUseCase;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@RestController
@RequestMapping("/api/qc-statuses")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class QcStatusController {

    QcStatusUseCase qcStatusUseCase;

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<QcStatusResponse>>> getQcStatuses(
            @ModelAttribute QcStatusSearchRequest request) {
        return ResponseEntity.ok(ApiResponse.success(qcStatusUseCase.getQcStatuses(request), "OK"));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Void>> createQcStatus(
            @Valid @RequestBody CreateQcStatusRequest request) {
        qcStatusUseCase.createQcStatus(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Created"));
    }
}
