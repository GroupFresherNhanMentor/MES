package fpt.qn.mes.inventory.application.mapper;

import org.mapstruct.Mapper;

import fpt.qn.mes.inventory.application.dto.lottype.LotTypeResponse;
import fpt.qn.mes.inventory.domain.entities.LotType;

@Mapper(componentModel = "spring")
public interface LotTypeDtoMapper {
    LotTypeResponse toDto(LotType entity);
    LotTypeResponse.UserInfo toUserInfo(LotType.UserRef ref);
}
