package fpt.qn.mes.master.line.application.mapper;

import org.mapstruct.Mapper;

import fpt.qn.mes.master.line.application.dto.response.ProductionLineDto;
import fpt.qn.mes.master.line.domain.entities.ProductionLine;

@Mapper(componentModel = "spring")
public interface LineDtoMapper {

    ProductionLineDto toDto(ProductionLine productionLine);
}
