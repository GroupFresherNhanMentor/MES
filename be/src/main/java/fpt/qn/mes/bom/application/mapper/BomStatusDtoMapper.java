package fpt.qn.mes.bom.application.mapper;

import org.mapstruct.Mapper;

import fpt.qn.mes.bom.application.dto.bomstatus.BomStatusResponse;
import fpt.qn.mes.bom.domain.entities.BomStatus;

@Mapper(componentModel = "spring")
public interface BomStatusDtoMapper {
    BomStatusResponse toDto(BomStatus bomStatus);
}
