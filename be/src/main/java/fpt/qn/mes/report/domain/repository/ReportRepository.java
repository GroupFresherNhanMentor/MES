package fpt.qn.mes.report.domain.repository;

import java.util.List;
import java.util.UUID;

import fpt.qn.mes.report.application.dto.response.InventorySummaryReportDto;

public interface ReportRepository {
    List<InventorySummaryReportDto> getInventorySummary(UUID warehouseId, UUID productTypeId, String productCode);
}
