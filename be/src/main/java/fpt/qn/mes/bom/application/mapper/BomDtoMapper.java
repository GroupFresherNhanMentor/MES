package fpt.qn.mes.bom.application.mapper;

import org.mapstruct.Mapper;

import fpt.qn.mes.bom.application.dto.response.BomResponse;
import fpt.qn.mes.bom.application.dto.response.BomItemResponse;
import fpt.qn.mes.bom.domain.entities.Bom;
import fpt.qn.mes.bom.domain.entities.BomItem;

@Mapper(componentModel = "spring")
public interface BomDtoMapper {

    BomResponse toDto(Bom bom);

    BomItemResponse toDto(BomItem bomItem);
}
