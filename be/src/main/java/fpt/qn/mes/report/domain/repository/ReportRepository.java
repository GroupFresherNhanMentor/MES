package fpt.qn.mes.report.domain.repository;

import java.util.List;
import fpt.qn.mes.common.domainQuery.PaginationResult;
import fpt.qn.mes.report.domain.entities.DefectRate;
import fpt.qn.mes.report.domain.entities.InventorySummary;
import fpt.qn.mes.report.domain.entities.MachineDowntime;
import fpt.qn.mes.report.domain.entities.MaterialShortage;
import fpt.qn.mes.report.domain.entities.ProductionOutput;
import fpt.qn.mes.report.domain.entities.StockMovementHistory;
import fpt.qn.mes.report.domain.repository.criteria.DefectRateSearchCriteria;
import fpt.qn.mes.report.domain.repository.criteria.InventorySummarySearchCriteria;
import fpt.qn.mes.report.domain.repository.criteria.MachineDowntimeSearchCriteria;
import fpt.qn.mes.report.domain.repository.criteria.ProductionOutputSearchCriteria;
import fpt.qn.mes.report.domain.repository.criteria.StockMovementHistorySearchCriteria;

public interface ReportRepository {
    PaginationResult<InventorySummary> getInventorySummary(InventorySummarySearchCriteria criteria);
    List<MaterialShortage> getMaterialShortages();
    PaginationResult<ProductionOutput> getProductionOutput(ProductionOutputSearchCriteria criteria);
    List<DefectRate> getDefectRates(DefectRateSearchCriteria criteria);
    List<MachineDowntime> getMachineDowntimes(MachineDowntimeSearchCriteria criteria);
    PaginationResult<StockMovementHistory> getStockMovementHistory(StockMovementHistorySearchCriteria criteria);
}
