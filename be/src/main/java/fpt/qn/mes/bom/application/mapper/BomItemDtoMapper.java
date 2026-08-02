package fpt.qn.mes.bom.application.mapper;

import org.mapstruct.Mapper;

import fpt.qn.mes.bom.application.dto.bomitem.BomItemResponse;
import fpt.qn.mes.bom.domain.entities.BomItem;

@Mapper(componentModel = "spring")
public interface BomItemDtoMapper {
    BomItemResponse toDto(BomItem bomItem);
}
