package fpt.qn.mes.inventory.application.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import fpt.qn.mes.inventory.application.dto.response.LocationSummaryDto;
import fpt.qn.mes.inventory.application.dto.response.MovementTypeSummaryDto;
import fpt.qn.mes.inventory.application.dto.response.ProductSummaryDto;
import fpt.qn.mes.inventory.application.dto.response.StockBalanceDto;
import fpt.qn.mes.inventory.application.dto.response.StockLotDto;
import fpt.qn.mes.inventory.application.dto.response.StockLotSummaryDto;
import fpt.qn.mes.inventory.application.dto.response.StockMovementDto;
import fpt.qn.mes.inventory.application.dto.response.StockStatusSummaryDto;
import fpt.qn.mes.inventory.application.dto.response.UserSummaryDto;
import fpt.qn.mes.inventory.application.dto.response.WarehouseSummaryDto;
import fpt.qn.mes.inventory.domain.entities.MovementType;
import fpt.qn.mes.inventory.domain.entities.StockBalance;
import fpt.qn.mes.inventory.domain.entities.StockLot;
import fpt.qn.mes.inventory.domain.entities.StockMovement;
import fpt.qn.mes.inventory.domain.entities.StockStatus;
import fpt.qn.mes.master.location.domain.entities.WarehouseLocation;
import fpt.qn.mes.master.product.domain.entities.Product;
import fpt.qn.mes.master.warehouse.domain.entities.Warehouse;
import fpt.qn.mes.user.domain.entities.User;

@Mapper(componentModel = "spring")
public interface InventoryDtoMapper {

    StockLotDto toDto(StockLot stockLot);

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
    StockMovementDto toDto(StockMovement stockMovement);

    StockBalanceDto toDto(StockBalance stockBalance);

    MovementTypeSummaryDto toSummary(MovementType entity);
    ProductSummaryDto toSummary(Product entity);
    StockLotSummaryDto toSummary(StockLot entity);
    WarehouseSummaryDto toSummary(Warehouse entity);
    LocationSummaryDto toSummary(WarehouseLocation entity);
    StockStatusSummaryDto toSummary(StockStatus entity);
    UserSummaryDto toSummary(User entity);
}
