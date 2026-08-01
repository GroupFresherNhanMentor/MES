package fpt.qn.mes.inventory.application.mapper;

import org.mapstruct.Mapper;

import fpt.qn.mes.inventory.application.dto.stockstatus.StockStatusResponse;
import fpt.qn.mes.inventory.domain.entities.StockStatus;

@Mapper(componentModel = "spring")
public interface StockStatusDtoMapper {
    StockStatusResponse toDto(StockStatus entity);
    StockStatusResponse.UserInfo toUserInfo(StockStatus.UserRef ref);
}
