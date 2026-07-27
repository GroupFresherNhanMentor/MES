package fpt.qn.mes.quality.application.mapper;

import org.mapstruct.Mapper;

import fpt.qn.mes.quality.application.dto.response.QualityInspectionDto;
import fpt.qn.mes.quality.application.dto.response.QualityInspectionResultDto;
import fpt.qn.mes.quality.domain.entities.QualityInspection;
import fpt.qn.mes.quality.domain.entities.QualityInspectionResult;

@Mapper(componentModel = "spring")
public interface QualityDtoMapper {

    QualityInspectionDto toDto(QualityInspection qualityInspection);

    QualityInspectionResultDto toDto(QualityInspectionResult qualityInspectionResult);
}
