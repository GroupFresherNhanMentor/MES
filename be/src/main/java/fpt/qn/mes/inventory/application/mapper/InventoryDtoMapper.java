package fpt.qn.mes.inventory.application.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import fpt.qn.mes.inventory.application.dto.warehouse.WarehouseLocationResponse;
import fpt.qn.mes.inventory.application.dto.product.ProductResponse;
import fpt.qn.mes.inventory.application.dto.stockbalance.StockBalanceResponse;
import fpt.qn.mes.inventory.application.dto.stocklot.StockLotResponse;
import fpt.qn.mes.inventory.application.dto.stocklot.StockLotSummaryResponse;
import fpt.qn.mes.inventory.application.dto.stockmovement.StockMovementResponse;
import fpt.qn.mes.inventory.application.dto.user.UserResponse;
import fpt.qn.mes.inventory.application.dto.warehouse.WarehouseResponse;
import fpt.qn.mes.inventory.domain.entities.StockBalance;
import fpt.qn.mes.inventory.domain.entities.StockLot;
import fpt.qn.mes.inventory.domain.entities.StockMovement;
import fpt.qn.mes.master.location.domain.entities.WarehouseLocation;
import fpt.qn.mes.master.product.domain.entities.Product;
import fpt.qn.mes.master.warehouse.domain.entities.Warehouse;
import fpt.qn.mes.user.domain.entities.User;

@Mapper(componentModel = "spring")
public interface InventoryDtoMapper {

    StockLotResponse toDto(StockLot stockLot);

    @Mapping(target = "movementType", source = "movementType")
    @Mapping(target = "product", ignore = true)
    @Mapping(target = "lot", source = "stockLot")
    @Mapping(target = "fromWarehouse", ignore = true)
    @Mapping(target = "fromLocation", ignore = true)
    @Mapping(target = "toWarehouse", ignore = true)
    @Mapping(target = "toLocation", ignore = true)
    @Mapping(target = "fromStatus", source = "fromStatus")
    @Mapping(target = "toStatus", source = "toStatus")
    @Mapping(target = "createdBy", ignore = true)
    StockMovementResponse toDto(StockMovement stockMovement);

    StockBalanceResponse toDto(StockBalance stockBalance);

    ProductResponse toSummary(Product entity);
    StockLotSummaryResponse toSummary(StockLot entity);
    WarehouseResponse toSummary(Warehouse entity);
    WarehouseLocationResponse toSummary(WarehouseLocation entity);
    UserResponse toSummary(User entity);
}
