package fpt.qn.mes.report.presentation;

import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import fpt.qn.mes.common.dto.response.ApiResponse;
import fpt.qn.mes.report.application.dto.response.InventorySummaryReportDto;
import fpt.qn.mes.report.application.port.in.ReportUseCase;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ReportController {

    ReportUseCase reportUseCase;

    @GetMapping("/inventory-summary")
    @PreAuthorize("hasAnyRole('ADMIN', 'FACTORY_MANAGER', 'AUDITOR')")
    public ResponseEntity<ApiResponse<List<InventorySummaryReportDto>>> getInventorySummary(
            @RequestParam(required = false) UUID warehouseId,
            @RequestParam(required = false) UUID productTypeId,
            @RequestParam(required = false) String productCode) {
        List<InventorySummaryReportDto> report = reportUseCase.getInventorySummary(warehouseId, productTypeId, productCode);
        return ResponseEntity.ok(ApiResponse.success(report, "OK"));
    }
}
