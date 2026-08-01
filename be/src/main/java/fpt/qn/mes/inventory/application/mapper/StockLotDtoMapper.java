package fpt.qn.mes.inventory.application.mapper;

import org.mapstruct.Mapper;

import fpt.qn.mes.inventory.application.dto.stocklot.StockLotResponse;
import fpt.qn.mes.inventory.domain.entities.LotType;
import fpt.qn.mes.inventory.domain.entities.StockLot;

@Mapper(componentModel = "spring")
public interface StockLotDtoMapper {
    StockLotResponse toDto(StockLot entity);
    StockLotResponse.ProductRef toProductRef(StockLot.ProductRef ref);
    StockLotResponse.LotTypeRef toLotTypeRef(LotType lotType);
    StockLotResponse.UserInfo toUserInfo(StockLot.UserRef ref);
}
