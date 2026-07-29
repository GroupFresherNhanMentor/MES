package fpt.qn.mes.report.application.service;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import fpt.qn.mes.report.application.dto.response.InventorySummaryReportDto;
import fpt.qn.mes.report.application.port.in.ReportUseCase;
import fpt.qn.mes.report.domain.repository.ReportRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ReportService implements ReportUseCase {

    ReportRepository reportRepository;

    @Override
    @Transactional(readOnly = true)
    public List<InventorySummaryReportDto> getInventorySummary(UUID warehouseId, UUID productTypeId, String productCode) {
        return reportRepository.getInventorySummary(warehouseId, productTypeId, productCode);
    }
}
