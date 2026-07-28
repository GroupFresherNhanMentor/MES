package fpt.qn.mes.quality.application.mapper;

import org.mapstruct.Mapper;

import fpt.qn.mes.quality.application.dto.response.DefectTypeDto;
import fpt.qn.mes.quality.domain.entities.DefectType;

@Mapper(componentModel = "spring")
public interface DefectTypeDtoMapper {
    DefectTypeDto toDto(DefectType defectType);
}
