package fpt.qn.mes.bom.application.mapper;

import org.mapstruct.Mapper;

import fpt.qn.mes.bom.application.dto.response.BomDto;
import fpt.qn.mes.bom.application.dto.response.BomItemDto;
import fpt.qn.mes.bom.domain.entities.Bom;
import fpt.qn.mes.bom.domain.entities.BomItem;

@Mapper(componentModel = "spring")
public interface BomDtoMapper {

    BomDto toDto(Bom bom);

    BomItemDto toDto(BomItem bomItem);
}
