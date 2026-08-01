package fpt.qn.mes.report.application.port.in;

import java.util.List;
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

public interface ReportUseCase {
    PageResponse<InventorySummaryResponse> getInventorySummary(InventorySummarySearchRequest request);
    List<MaterialShortageResponse> getMaterialShortages();
    PageResponse<ProductionOutputResponse> getProductionOutput(ProductionOutputSearchRequest request);
    List<DefectRateResponse> getDefectRates(DefectRateSearchRequest request);
    List<MachineDowntimeResponse> getMachineDowntimes(MachineDowntimeSearchRequest request);
    PageResponse<StockMovementHistoryResponse> getStockMovementHistory(StockMovementHistorySearchRequest request);
}
