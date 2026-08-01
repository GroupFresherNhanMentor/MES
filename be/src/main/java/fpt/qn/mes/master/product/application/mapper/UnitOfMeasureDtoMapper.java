package fpt.qn.mes.master.product.application.mapper;

import org.mapstruct.Mapper;

import fpt.qn.mes.master.product.application.dto.unitofmeasure.UnitOfMeasureResponse;
import fpt.qn.mes.master.product.domain.entities.UnitOfMeasure;

@Mapper(componentModel = "spring")
public interface UnitOfMeasureDtoMapper {
    UnitOfMeasureResponse toDto(UnitOfMeasure unit);
}
