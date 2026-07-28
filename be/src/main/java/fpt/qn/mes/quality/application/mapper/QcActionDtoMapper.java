package fpt.qn.mes.quality.application.mapper;

import org.mapstruct.Mapper;

import fpt.qn.mes.quality.application.dto.response.QcActionDto;
import fpt.qn.mes.quality.domain.entities.QcAction;

@Mapper(componentModel = "spring")
public interface QcActionDtoMapper {
    QcActionDto toDto(QcAction action);
}
