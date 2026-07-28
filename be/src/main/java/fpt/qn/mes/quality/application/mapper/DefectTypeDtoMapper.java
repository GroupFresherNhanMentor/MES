package fpt.qn.mes.quality.application.mapper;

import org.mapstruct.Mapper;

import fpt.qn.mes.quality.application.dto.defecttype.DefectTypeResponse;
import fpt.qn.mes.quality.domain.entities.DefectType;

@Mapper(componentModel = "spring")
public interface DefectTypeDtoMapper {
    DefectTypeResponse toDto(DefectType defectType);
}
