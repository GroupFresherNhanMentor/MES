package fpt.qn.mes.quality.application.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import fpt.qn.mes.quality.application.dto.response.QualityInspectionDto;
import fpt.qn.mes.quality.application.dto.response.QualityInspectionResultDto;
import fpt.qn.mes.quality.domain.entities.QualityInspection;
import fpt.qn.mes.quality.domain.entities.QualityInspectionResult;

@Mapper(componentModel = "spring")
public interface QualityDtoMapper {

    @Mapping(target = "productCode", ignore = true)
    @Mapping(target = "lotNumber", ignore = true)
    @Mapping(target = "qcStatusName", ignore = true)
    @Mapping(target = "remainingQuantity", ignore = true)
    QualityInspectionDto toDto(QualityInspection qualityInspection);

    QualityInspectionResultDto toDto(QualityInspectionResult qualityInspectionResult);
}