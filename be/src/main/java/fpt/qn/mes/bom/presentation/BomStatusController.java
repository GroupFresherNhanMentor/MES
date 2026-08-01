package fpt.qn.mes.bom.presentation;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import fpt.qn.mes.bom.application.dto.bomstatus.BomStatusResponse;
import fpt.qn.mes.bom.application.dto.bomstatus.create.CreateBomStatusRequest;
import fpt.qn.mes.bom.application.port.in.BomStatusUseCase;
import fpt.qn.mes.common.dto.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@RestController
@RequestMapping("/api/bom-statuses")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class BomStatusController {

    BomStatusUseCase bomStatusUseCase;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<List<BomStatusResponse>>> getBomStatuses() {
        return ResponseEntity.ok(ApiResponse.success(bomStatusUseCase.getBomStatuses(), "OK"));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> createBomStatus(@Valid @RequestBody CreateBomStatusRequest request) {
        bomStatusUseCase.createBomStatus(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Created"));
    }
}
