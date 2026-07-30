package fpt.qn.mes.quality.application.mapper;

import org.mapstruct.Mapper;

import fpt.qn.mes.quality.application.dto.qcaction.QcActionResponse;
import fpt.qn.mes.quality.domain.entities.QcAction;

@Mapper(componentModel = "spring")
public interface QcActionDtoMapper {
    QcActionResponse toDto(QcAction action);
}
