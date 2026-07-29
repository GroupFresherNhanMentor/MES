package fpt.qn.mes.report.application.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import fpt.qn.mes.report.application.dto.response.InventorySummaryReportDto;
import fpt.qn.mes.report.domain.repository.ReportRepository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReportServiceTest {

    @Mock
    ReportRepository reportRepository;

    @InjectMocks
    ReportService reportService;

    @Test
    void getInventorySummary_HappyPath_ReturnsSummaryList() {
        UUID warehouseId = UUID.randomUUID();
        UUID productTypeId = UUID.randomUUID();
        String productCode = "PROD-001";

        InventorySummaryReportDto row1 = InventorySummaryReportDto.builder()
                .productCode("PROD-001")
                .productName("Steel Sheet 2mm")
                .warehouseName("Main Warehouse")
                .availableQuantity(new BigDecimal("100.00"))
                .reservedQuantity(new BigDecimal("20.00"))
                .qualityInspectionQuantity(new BigDecimal("5.00"))
                .onHoldQuantity(BigDecimal.ZERO)
                .scrappedQuantity(new BigDecimal("2.00"))
                .totalOnHand(new BigDecimal("127.00"))
                .build();

        when(reportRepository.getInventorySummary(warehouseId, productTypeId, productCode))
                .thenReturn(List.of(row1));

        List<InventorySummaryReportDto> result = reportService.getInventorySummary(warehouseId, productTypeId, productCode);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("PROD-001", result.get(0).getProductCode());
        assertEquals(new BigDecimal("127.00"), result.get(0).getTotalOnHand());
        verify(reportRepository).getInventorySummary(warehouseId, productTypeId, productCode);
    }
}
