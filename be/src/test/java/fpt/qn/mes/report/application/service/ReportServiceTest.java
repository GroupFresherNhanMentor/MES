package fpt.qn.mes.report.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import fpt.qn.mes.common.domainQuery.PaginationResult;
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
import fpt.qn.mes.report.application.mapper.ReportDtoMapper;
import fpt.qn.mes.report.domain.entities.DefectRate;
import fpt.qn.mes.report.domain.entities.InventorySummary;
import fpt.qn.mes.report.domain.entities.MachineDowntime;
import fpt.qn.mes.report.domain.entities.MaterialShortage;
import fpt.qn.mes.report.domain.entities.ProductionOutput;
import fpt.qn.mes.report.domain.entities.StockMovementHistory;
import fpt.qn.mes.report.domain.repository.ReportRepository;

@ExtendWith(MockitoExtension.class)
class ReportServiceTest {

    @Mock
    ReportRepository reportRepository;

    @Mock
    ReportDtoMapper mapper;

    @InjectMocks
    ReportService reportService;

    @Test
    void getInventorySummary_shouldReturnPageResponse() {
        InventorySummary entity = InventorySummary.builder()
                .productId(UUID.randomUUID())
                .productCode("P001")
                .productName("Widget")
                .availableQuantity(BigDecimal.TEN)
                .totalOnHand(BigDecimal.TEN)
                .build();
        InventorySummaryResponse dto = InventorySummaryResponse.builder()
                .productId(entity.getProductId())
                .productCode("P001")
                .productName("Widget")
                .availableQuantity(BigDecimal.TEN)
                .totalOnHand(BigDecimal.TEN)
                .build();

        when(reportRepository.getInventorySummary(any())).thenReturn(PaginationResult.<InventorySummary>builder()
                .items(List.of(entity))
                .total(1L)
                .build());
        when(mapper.toDto(entity)).thenReturn(dto);

        InventorySummarySearchRequest req = new InventorySummarySearchRequest();
        req.setPage(0);
        req.setSize(10);

        PageResponse<InventorySummaryResponse> res = reportService.getInventorySummary(req);

        assertThat(res.getItems()).hasSize(1);
        assertThat(res.getTotalElements()).isEqualTo(1L);
        verify(reportRepository).getInventorySummary(any());
    }

    @Test
    void getMaterialShortages_shouldReturnList() {
        MaterialShortage entity = MaterialShortage.builder()
                .workOrderCode("WO-100")
                .materialCode("MAT-01")
                .shortageQuantity(BigDecimal.valueOf(5))
                .build();
        MaterialShortageResponse dto = MaterialShortageResponse.builder()
                .workOrderCode("WO-100")
                .materialCode("MAT-01")
                .shortageQuantity(BigDecimal.valueOf(5))
                .build();

        when(reportRepository.getMaterialShortages()).thenReturn(List.of(entity));
        when(mapper.toDto(entity)).thenReturn(dto);

        List<MaterialShortageResponse> res = reportService.getMaterialShortages();

        assertThat(res).hasSize(1);
        assertThat(res.get(0).getWorkOrderCode()).isEqualTo("WO-100");
    }

    @Test
    void getProductionOutput_shouldReturnPageResponse() {
        ProductionOutput entity = ProductionOutput.builder()
                .workOrderCode("WO-100")
                .productCode("P-01")
                .plannedQuantity(BigDecimal.valueOf(100))
                .actualQuantity(BigDecimal.valueOf(90))
                .build();
        ProductionOutputResponse dto = ProductionOutputResponse.builder()
                .workOrderCode("WO-100")
                .productCode("P-01")
                .plannedQuantity(BigDecimal.valueOf(100))
                .actualQuantity(BigDecimal.valueOf(90))
                .build();

        when(reportRepository.getProductionOutput(any())).thenReturn(PaginationResult.<ProductionOutput>builder()
                .items(List.of(entity))
                .total(1L)
                .build());
        when(mapper.toDto(entity)).thenReturn(dto);

        ProductionOutputSearchRequest req = new ProductionOutputSearchRequest();
        req.setPage(0);
        req.setSize(10);

        PageResponse<ProductionOutputResponse> res = reportService.getProductionOutput(req);

        assertThat(res.getItems()).hasSize(1);
        verify(reportRepository).getProductionOutput(any());
    }

    @Test
    void getDefectRates_shouldReturnList() {
        DefectRate entity = DefectRate.builder()
                .productCode("P-01")
                .defectRate(BigDecimal.valueOf(2.5))
                .topDefectTypes(List.of("SCRATCH"))
                .build();
        DefectRateResponse dto = DefectRateResponse.builder()
                .productCode("P-01")
                .defectRate(BigDecimal.valueOf(2.5))
                .topDefectTypes(List.of("SCRATCH"))
                .build();

        when(reportRepository.getDefectRates(any())).thenReturn(List.of(entity));
        when(mapper.toDto(entity)).thenReturn(dto);

        DefectRateSearchRequest req = new DefectRateSearchRequest();

        List<DefectRateResponse> res = reportService.getDefectRates(req);

        assertThat(res).hasSize(1);
        assertThat(res.get(0).getTopDefectTypes()).contains("SCRATCH");
    }

    @Test
    void getMachineDowntimes_shouldReturnList() {
        MachineDowntime entity = MachineDowntime.builder()
                .machineCode("M-01")
                .totalDowntimeMinutes(120L)
                .build();
        MachineDowntimeResponse dto = MachineDowntimeResponse.builder()
                .machineCode("M-01")
                .totalDowntimeMinutes(120L)
                .build();

        when(reportRepository.getMachineDowntimes(any())).thenReturn(List.of(entity));
        when(mapper.toDto(entity)).thenReturn(dto);

        MachineDowntimeSearchRequest req = new MachineDowntimeSearchRequest();

        List<MachineDowntimeResponse> res = reportService.getMachineDowntimes(req);

        assertThat(res).hasSize(1);
        assertThat(res.get(0).getMachineCode()).isEqualTo("M-01");
    }

    @Test
    void getStockMovementHistory_shouldReturnPageResponse() {
        StockMovementHistory entity = StockMovementHistory.builder()
                .productCode("P-01")
                .movementType("TRANSFER")
                .quantity(BigDecimal.TEN)
                .build();
        StockMovementHistoryResponse dto = StockMovementHistoryResponse.builder()
                .productCode("P-01")
                .movementType("TRANSFER")
                .quantity(BigDecimal.TEN)
                .build();

        when(reportRepository.getStockMovementHistory(any())).thenReturn(PaginationResult.<StockMovementHistory>builder()
                .items(List.of(entity))
                .total(1L)
                .build());
        when(mapper.toDto(entity)).thenReturn(dto);

        StockMovementHistorySearchRequest req = new StockMovementHistorySearchRequest();
        req.setPage(0);
        req.setSize(10);

        PageResponse<StockMovementHistoryResponse> res = reportService.getStockMovementHistory(req);

        assertThat(res.getItems()).hasSize(1);
        verify(reportRepository).getStockMovementHistory(any());
    }
}
