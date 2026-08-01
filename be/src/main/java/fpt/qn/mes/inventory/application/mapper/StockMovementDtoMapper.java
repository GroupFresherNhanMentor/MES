package fpt.qn.mes.inventory.application.mapper;

import org.mapstruct.Mapper;

import fpt.qn.mes.inventory.application.dto.stockmovement.StockMovementResponse;
import fpt.qn.mes.inventory.domain.entities.StockMovement;

import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface StockMovementDtoMapper {

    @Mapping(target = "movementType", ignore = true)
    @Mapping(target = "product", ignore = true)
    @Mapping(target = "lot", ignore = true)
    @Mapping(target = "fromWarehouse", ignore = true)
    @Mapping(target = "toWarehouse", ignore = true)
    @Mapping(target = "fromLocation", ignore = true)
    @Mapping(target = "toLocation", ignore = true)
    @Mapping(target = "fromStatus", ignore = true)
    @Mapping(target = "toStatus", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    StockMovementResponse toDto(StockMovement entity);
}
