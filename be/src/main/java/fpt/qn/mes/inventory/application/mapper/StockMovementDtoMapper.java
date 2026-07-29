package fpt.qn.mes.inventory.application.mapper;

import org.mapstruct.Mapper;

import fpt.qn.mes.inventory.application.dto.response.StockMovementDto;
import fpt.qn.mes.inventory.domain.entities.StockMovement;

@Mapper(componentModel = "spring")
public interface StockMovementDtoMapper {
    StockMovementDto toDto(StockMovement entity);
}
