package fpt.qn.mes.inventory.application.mapper;

import org.springframework.stereotype.Component;

import fpt.qn.mes.inventory.application.dto.StockBalanceDto;
import fpt.qn.mes.inventory.application.dto.StockLotDto;
import fpt.qn.mes.inventory.application.dto.StockMovementDto;
import fpt.qn.mes.inventory.domain.entities.StockBalance;
import fpt.qn.mes.inventory.domain.entities.StockLot;
import fpt.qn.mes.inventory.domain.entities.StockMovement;

@Component
public class InventoryDtoMapper {

    public StockLotDto toDto(StockLot l) {
        return null;
    }

    public StockMovementDto toDto(StockMovement m) {
        return null;
    }

    public StockBalanceDto toDto(StockBalance b) {
        return null;
    }
}
