package fpt.qn.mes.quality.application.mapper;

import org.mapstruct.Mapper;

import fpt.qn.mes.quality.application.dto.qcstatus.QcStatusResponse;
import fpt.qn.mes.quality.domain.entities.QcStatus;

@Mapper(componentModel = "spring")
public interface QcStatusDtoMapper {
    QcStatusResponse toDto(QcStatus status);
}
