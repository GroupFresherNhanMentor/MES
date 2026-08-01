package fpt.qn.mes.master.line.presentation;

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
import fpt.qn.mes.master.line.application.dto.linestatus.LineStatusResponse;
import fpt.qn.mes.master.line.application.dto.linestatus.create.CreateLineStatusRequest;
import fpt.qn.mes.master.line.application.dto.linestatus.search.LineStatusSearchRequest;
import fpt.qn.mes.master.line.application.port.in.LineStatusUseCase;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@RestController
@RequestMapping("/api/line-statuses")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class LineStatusController {

    LineStatusUseCase linestatusUseCase;

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<LineStatusResponse>>> getLineStatuses(
            @ModelAttribute LineStatusSearchRequest request) {
        return ResponseEntity.ok(ApiResponse.success(linestatusUseCase.getLineStatuses(request), "OK"));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Void>> createLineStatus(
            @Valid @RequestBody CreateLineStatusRequest request) {
        linestatusUseCase.createLineStatus(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Created"));
    }
}
