package fpt.qn.mes.inventory.application.mapper;

import org.mapstruct.Mapper;

import fpt.qn.mes.inventory.application.dto.response.StockLotDto;
import fpt.qn.mes.inventory.domain.entities.StockLot;

@Mapper(componentModel = "spring")
public interface StockLotDtoMapper {
    StockLotDto toDto(StockLot entity);
}
