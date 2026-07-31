package fpt.qn.mes.inventory.application.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import fpt.qn.mes.inventory.application.dto.stockadjustmentapproval.StockAdjustmentApprovalResponse;
import fpt.qn.mes.inventory.domain.entities.StockAdjustmentApproval;

@Mapper(componentModel = "spring")
public interface StockAdjustmentApprovalDtoMapper {

    @Mapping(target = "product", ignore = true)
    @Mapping(target = "warehouse", ignore = true)
    @Mapping(target = "location", ignore = true)
    @Mapping(target = "creator", ignore = true)
    StockAdjustmentApprovalResponse toDto(StockAdjustmentApproval approval);
}
