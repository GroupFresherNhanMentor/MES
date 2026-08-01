package fpt.qn.mes.bom.application.mapper;

import org.mapstruct.Mapper;

import fpt.qn.mes.bom.application.dto.bom.BomResponse;
import fpt.qn.mes.bom.domain.entities.Bom;
import fpt.qn.mes.bom.domain.entities.BomStatus;

@Mapper(componentModel = "spring")
public interface BomDtoMapper {
    BomResponse toDto(Bom bom);
    BomResponse.BomStatusInfo toStatusInfo(BomStatus bomStatus);
    BomResponse.UserInfo toUserInfo(Bom.UserRef userRef);
}
