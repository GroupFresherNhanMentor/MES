package fpt.qn.mes.inventory.application.mapper;

import org.mapstruct.Mapper;

import fpt.qn.mes.inventory.application.dto.response.StockBalanceDto;
import fpt.qn.mes.inventory.application.dto.response.StockLotDto;
import fpt.qn.mes.inventory.application.dto.response.StockMovementDto;
import fpt.qn.mes.inventory.domain.entities.StockBalance;
import fpt.qn.mes.inventory.domain.entities.StockLot;
import fpt.qn.mes.inventory.domain.entities.StockMovement;

import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface InventoryDtoMapper {

    StockLotDto toDto(StockLot stockLot);

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
    StockMovementDto toDto(StockMovement stockMovement);

    StockBalanceDto toDto(StockBalance stockBalance);
}
