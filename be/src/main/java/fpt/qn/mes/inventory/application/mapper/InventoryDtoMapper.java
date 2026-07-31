package fpt.qn.mes.inventory.application.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import fpt.qn.mes.inventory.application.dto.product.ProductResponse;
import fpt.qn.mes.inventory.application.dto.stockbalance.StockBalanceResponse;
import fpt.qn.mes.inventory.application.dto.stockmovement.StockMovementResponse;
import fpt.qn.mes.inventory.application.dto.user.UserResponse;
import fpt.qn.mes.inventory.application.dto.warehouse.WarehouseLocationResponse;
import fpt.qn.mes.inventory.application.dto.warehouse.WarehouseResponse;
import fpt.qn.mes.inventory.domain.entities.StockBalance;
import fpt.qn.mes.inventory.domain.entities.StockMovement;

@Mapper(componentModel = "spring")
public interface InventoryDtoMapper {

    @Mapping(target = "lot", source = "stockLot")
    @Mapping(target = "createdBy", source = "createdByUser")
    StockMovementResponse toDto(StockMovement movement);

    StockBalanceResponse toDto(StockBalance balance);

    ProductResponse toProductResponse(StockMovement.ProductRef ref);

    WarehouseResponse toWarehouseResponse(StockMovement.WarehouseRef ref);

    WarehouseLocationResponse toLocationResponse(StockMovement.WarehouseLocationRef ref);

    UserResponse toUserResponse(StockMovement.UserRef ref);
}
