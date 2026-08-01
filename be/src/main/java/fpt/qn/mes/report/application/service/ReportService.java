package fpt.qn.mes.report.application.service;

import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import fpt.qn.mes.common.dto.response.PageResponse;
import fpt.qn.mes.common.domainQuery.PaginationResult;
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
import fpt.qn.mes.report.application.port.in.ReportUseCase;
import fpt.qn.mes.report.domain.entities.DefectRate;
import fpt.qn.mes.report.domain.entities.InventorySummary;
import fpt.qn.mes.report.domain.entities.MachineDowntime;
import fpt.qn.mes.report.domain.entities.MaterialShortage;
import fpt.qn.mes.report.domain.entities.ProductionOutput;
import fpt.qn.mes.report.domain.entities.StockMovementHistory;
import fpt.qn.mes.report.domain.repository.ReportRepository;
import fpt.qn.mes.report.domain.repository.criteria.DefectRateSearchCriteria;
import fpt.qn.mes.report.domain.repository.criteria.InventorySummarySearchCriteria;
import fpt.qn.mes.report.domain.repository.criteria.MachineDowntimeSearchCriteria;
import fpt.qn.mes.report.domain.repository.criteria.ProductionOutputSearchCriteria;
import fpt.qn.mes.report.domain.repository.criteria.StockMovementHistorySearchCriteria;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Transactional(readOnly = true)
public class ReportService implements ReportUseCase {

    ReportRepository reportRepository;
    ReportDtoMapper mapper;

    @Override
    public PageResponse<InventorySummaryResponse> getInventorySummary(InventorySummarySearchRequest request) {
        InventorySummarySearchCriteria criteria = InventorySummarySearchCriteria.builder()
                .warehouseId(request.getWarehouseId())
                .productType(request.getProductType())
                .productCode(request.getProductCode())
                .page(request.getPage())
                .size(request.getSize())
                .sort(request.getSort())
                .build();
        PaginationResult<InventorySummary> result = reportRepository.getInventorySummary(criteria);
        List<InventorySummaryResponse> content = result.getItems().stream()
                .map(mapper::toDto)
                .toList();
        return PageResponse.of(content, result.getTotal(), request.getPage(), request.getSize());
    }

    @Override
    public List<MaterialShortageResponse> getMaterialShortages() {
        List<MaterialShortage> shortages = reportRepository.getMaterialShortages();
        return shortages.stream()
                .map(mapper::toDto)
                .toList();
    }

    @Override
    public PageResponse<ProductionOutputResponse> getProductionOutput(ProductionOutputSearchRequest request) {
        ProductionOutputSearchCriteria criteria = ProductionOutputSearchCriteria.builder()
                .fromDate(request.getFromDate())
                .toDate(request.getToDate())
                .page(request.getPage())
                .size(request.getSize())
                .sort(request.getSort())
                .build();
        PaginationResult<ProductionOutput> result = reportRepository.getProductionOutput(criteria);
        List<ProductionOutputResponse> content = result.getItems().stream()
                .map(mapper::toDto)
                .toList();
        return PageResponse.of(content, result.getTotal(), request.getPage(), request.getSize());
    }

    @Override
    public List<DefectRateResponse> getDefectRates(DefectRateSearchRequest request) {
        DefectRateSearchCriteria criteria = DefectRateSearchCriteria.builder()
                .fromDate(request.getFromDate())
                .toDate(request.getToDate())
                .build();
        List<DefectRate> defectRates = reportRepository.getDefectRates(criteria);
        return defectRates.stream()
                .map(mapper::toDto)
                .toList();
    }

    @Override
    public List<MachineDowntimeResponse> getMachineDowntimes(MachineDowntimeSearchRequest request) {
        MachineDowntimeSearchCriteria criteria = MachineDowntimeSearchCriteria.builder()
                .fromDate(request.getFromDate())
                .toDate(request.getToDate())
                .build();
        List<MachineDowntime> downtimes = reportRepository.getMachineDowntimes(criteria);
        return downtimes.stream()
                .map(mapper::toDto)
                .toList();
    }

    @Override
    public PageResponse<StockMovementHistoryResponse> getStockMovementHistory(StockMovementHistorySearchRequest request) {
        StockMovementHistorySearchCriteria criteria = StockMovementHistorySearchCriteria.builder()
                .productId(request.getProductId())
                .warehouseId(request.getWarehouseId())
                .movementTypeId(request.getMovementTypeId())
                .fromDate(request.getFromDate())
                .toDate(request.getToDate())
                .referenceType(request.getReferenceType())
                .referenceId(request.getReferenceId())
                .page(request.getPage())
                .size(request.getSize())
                .sort(request.getSort())
                .build();
        PaginationResult<StockMovementHistory> result = reportRepository.getStockMovementHistory(criteria);
        List<StockMovementHistoryResponse> content = result.getItems().stream()
                .map(mapper::toDto)
                .toList();
        return PageResponse.of(content, result.getTotal(), request.getPage(), request.getSize());
    }
}
