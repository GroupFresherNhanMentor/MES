package fpt.qn.mes.inventory.application.mapper;

import org.mapstruct.Mapper;

import fpt.qn.mes.inventory.application.dto.response.StockBalanceDto;
import fpt.qn.mes.inventory.domain.entities.StockBalance;

@Mapper(componentModel = "spring")
public interface StockBalanceDtoMapper {
    StockBalanceDto toDto(StockBalance entity);
}
