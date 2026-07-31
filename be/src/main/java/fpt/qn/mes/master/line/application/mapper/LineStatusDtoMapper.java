package fpt.qn.mes.master.line.application.mapper;

import org.mapstruct.Mapper;
import fpt.qn.mes.master.line.application.dto.linestatus.LineStatusResponse;
import fpt.qn.mes.master.line.domain.entities.LineStatus;

@Mapper(componentModel = "spring")
public interface LineStatusDtoMapper {
    LineStatusResponse toDto(LineStatus status);
}
