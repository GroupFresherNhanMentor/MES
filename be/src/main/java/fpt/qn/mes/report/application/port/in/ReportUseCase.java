package fpt.qn.mes.report.application.port.in;

import java.util.List;
import java.util.UUID;

import fpt.qn.mes.report.application.dto.response.InventorySummaryReportDto;

public interface ReportUseCase {
    List<InventorySummaryReportDto> getInventorySummary(UUID warehouseId, UUID productTypeId, String productCode);
}
