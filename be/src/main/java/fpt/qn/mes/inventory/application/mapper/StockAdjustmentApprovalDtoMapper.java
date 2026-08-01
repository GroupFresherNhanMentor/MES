package fpt.qn.mes.inventory.application.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import fpt.qn.mes.inventory.application.dto.product.ProductResponse;
import fpt.qn.mes.inventory.application.dto.stockadjustmentapproval.StockAdjustmentApprovalResponse;
import fpt.qn.mes.inventory.application.dto.user.UserResponse;
import fpt.qn.mes.inventory.application.dto.warehouse.WarehouseLocationResponse;
import fpt.qn.mes.inventory.application.dto.warehouse.WarehouseResponse;
import fpt.qn.mes.inventory.domain.entities.StockAdjustmentApproval;

@Mapper(componentModel = "spring")
public interface StockAdjustmentApprovalDtoMapper {

    @Mapping(target = "creator", source = "createdByUser")
    StockAdjustmentApprovalResponse toDto(StockAdjustmentApproval approval);

    ProductResponse toProductResponse(StockAdjustmentApproval.ProductRef ref);

    WarehouseResponse toWarehouseResponse(StockAdjustmentApproval.WarehouseRef ref);

    WarehouseLocationResponse toLocationResponse(StockAdjustmentApproval.WarehouseLocationRef ref);

    UserResponse toUserResponse(StockAdjustmentApproval.UserRef ref);
}
