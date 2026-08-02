package fpt.qn.mes.report.presentation;

import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import fpt.qn.mes.common.dto.response.ApiResponse;
import fpt.qn.mes.common.dto.response.PageResponse;
import fpt.qn.mes.report.application.dto.defectrate.DefectRateResponse;
import fpt.qn.mes.report.application.dto.defectrate.search.DefectRateSearchRequest;
import fpt.qn.mes.report.application.dto.inventorysummary.InventorySummaryResponse;
import fpt.qn.mes.report.application.dto.inventorysummary.search.InventorySummarySearchRequest;
import fpt.qn.mes.report.application.dto.machinedowntime.MachineDowntimeResponse;
import fpt.qn.mes.report.application.dto.machinedowntime.search.MachineDowntimeSearchRequest;
import fpt.qn.mes.report.application.dto.materialshortage.MaterialShortageResponse;
import fpt.qn.mes.report.application.dto.productionoutput.ProductionOutputResponse;
import fpt.qn.mes.report.application.dto.productionoutput.search.ProductionOutputSearchRequest;
import fpt.qn.mes.report.application.dto.stockmovementhistory.StockMovementHistoryResponse;
import fpt.qn.mes.report.application.dto.stockmovementhistory.search.StockMovementHistorySearchRequest;
import fpt.qn.mes.report.application.port.in.ReportUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Tag(name = "Reports", description = "Factory operational report APIs")
@SecurityRequirement(name = "bearerAuth")
public class ReportController {

    ReportUseCase reportUseCase;

    @GetMapping("/inventory-summary")
    @PreAuthorize("hasAnyRole('FACTORY_MANAGER', 'ADMIN', 'AUDITOR')")
    @Operation(summary = "Get inventory summary report", description = "View total stock balances aggregated by product and warehouse across stock statuses")
    public ResponseEntity<ApiResponse<PageResponse<InventorySummaryResponse>>> getInventorySummary(
            @ModelAttribute @Valid InventorySummarySearchRequest request) {
        PageResponse<InventorySummaryResponse> result = reportUseCase.getInventorySummary(request);
        return ResponseEntity.ok(ApiResponse.success(result, "OK"));
    }

    @GetMapping("/material-shortage")
    @PreAuthorize("hasAnyRole('FACTORY_MANAGER', 'ADMIN', 'AUDITOR')")
    @Operation(summary = "Get material shortage report", description = "View current material shortages across active work orders")
    public ResponseEntity<ApiResponse<List<MaterialShortageResponse>>> getMaterialShortages() {
        List<MaterialShortageResponse> result = reportUseCase.getMaterialShortages();
        return ResponseEntity.ok(ApiResponse.success(result, "OK"));
    }

    @GetMapping("/production-output")
    @PreAuthorize("hasAnyRole('FACTORY_MANAGER', 'ADMIN', 'AUDITOR')")
    @Operation(summary = "Get production output report", description = "View daily production quantities and completion rates by work order and product")
    public ResponseEntity<ApiResponse<PageResponse<ProductionOutputResponse>>> getProductionOutput(
            @ModelAttribute @Valid ProductionOutputSearchRequest request) {
        PageResponse<ProductionOutputResponse> result = reportUseCase.getProductionOutput(request);
        return ResponseEntity.ok(ApiResponse.success(result, "OK"));
    }

    @GetMapping("/defect-rate")
    @PreAuthorize("hasAnyRole('FACTORY_MANAGER', 'ADMIN', 'AUDITOR')")
    @Operation(summary = "Get defect rate report", description = "View product quality inspection defect rates and top defect types")
    public ResponseEntity<ApiResponse<List<DefectRateResponse>>> getDefectRates(
            @ModelAttribute @Valid DefectRateSearchRequest request) {
        List<DefectRateResponse> result = reportUseCase.getDefectRates(request);
        return ResponseEntity.ok(ApiResponse.success(result, "OK"));
    }

    @GetMapping("/machine-downtime")
    @PreAuthorize("hasAnyRole('FACTORY_MANAGER', 'ADMIN', 'AUDITOR')")
    @Operation(summary = "Get machine downtime report", description = "View total machine downtime minutes, maintenance ticket counts, and last downtime reasons")
    public ResponseEntity<ApiResponse<List<MachineDowntimeResponse>>> getMachineDowntimes(
            @ModelAttribute @Valid MachineDowntimeSearchRequest request) {
        List<MachineDowntimeResponse> result = reportUseCase.getMachineDowntimes(request);
        return ResponseEntity.ok(ApiResponse.success(result, "OK"));
    }

    @GetMapping("/stock-movement-history")
    @PreAuthorize("hasAnyRole('FACTORY_MANAGER', 'ADMIN', 'AUDITOR')")
    @Operation(summary = "Get stock movement history report", description = "View detailed historical stock movements ledger with criteria filters")
    public ResponseEntity<ApiResponse<PageResponse<StockMovementHistoryResponse>>> getStockMovementHistory(
            @ModelAttribute @Valid StockMovementHistorySearchRequest request) {
        PageResponse<StockMovementHistoryResponse> result = reportUseCase.getStockMovementHistory(request);
        return ResponseEntity.ok(ApiResponse.success(result, "OK"));
    }
}
