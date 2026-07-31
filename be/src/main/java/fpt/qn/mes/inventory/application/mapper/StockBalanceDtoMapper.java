package fpt.qn.mes.inventory.application.mapper;

import org.mapstruct.Mapper;

import fpt.qn.mes.inventory.application.dto.stockbalance.StockBalanceResponse;
import fpt.qn.mes.inventory.domain.entities.StockBalance;

@Mapper(componentModel = "spring")
public interface StockBalanceDtoMapper {
    StockBalanceResponse toDto(StockBalance entity);
}
