package fpt.qn.mes.report.application.mapper;

import org.mapstruct.Mapper;
import fpt.qn.mes.report.application.dto.defectrate.DefectRateResponse;
import fpt.qn.mes.report.application.dto.inventorysummary.InventorySummaryResponse;
import fpt.qn.mes.report.application.dto.machinedowntime.MachineDowntimeResponse;
import fpt.qn.mes.report.application.dto.materialshortage.MaterialShortageResponse;
import fpt.qn.mes.report.application.dto.productionoutput.ProductionOutputResponse;
import fpt.qn.mes.report.application.dto.stockmovementhistory.StockMovementHistoryResponse;
import fpt.qn.mes.report.domain.entities.DefectRate;
import fpt.qn.mes.report.domain.entities.InventorySummary;
import fpt.qn.mes.report.domain.entities.MachineDowntime;
import fpt.qn.mes.report.domain.entities.MaterialShortage;
import fpt.qn.mes.report.domain.entities.ProductionOutput;
import fpt.qn.mes.report.domain.entities.StockMovementHistory;

@Mapper(componentModel = "spring")
public interface ReportDtoMapper {
    InventorySummaryResponse toDto(InventorySummary entity);
    MaterialShortageResponse toDto(MaterialShortage entity);
    ProductionOutputResponse toDto(ProductionOutput entity);
    DefectRateResponse toDto(DefectRate entity);
    MachineDowntimeResponse toDto(MachineDowntime entity);
    StockMovementHistoryResponse toDto(StockMovementHistory entity);
}
