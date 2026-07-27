package fpt.qn.mes.inventory.application.mapper;

import org.mapstruct.Mapper;

import fpt.qn.mes.inventory.application.dto.response.StockBalanceDto;
import fpt.qn.mes.inventory.application.dto.response.StockLotDto;
import fpt.qn.mes.inventory.application.dto.response.StockMovementDto;
import fpt.qn.mes.inventory.domain.entities.StockBalance;
import fpt.qn.mes.inventory.domain.entities.StockLot;
import fpt.qn.mes.inventory.domain.entities.StockMovement;

@Mapper(componentModel = "spring")
public interface InventoryDtoMapper {

    StockLotDto toDto(StockLot stockLot);

    StockMovementDto toDto(StockMovement stockMovement);

    StockBalanceDto toDto(StockBalance stockBalance);
}
