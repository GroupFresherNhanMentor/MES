package fpt.qn.mes.master.line.application.mapper;

import org.mapstruct.Mapper;
import fpt.qn.mes.master.line.application.dto.line.LineResponse;
import fpt.qn.mes.master.line.domain.entities.Line;

@Mapper(componentModel = "spring", uses = {LineStatusDtoMapper.class})
public interface LineDtoMapper {
    LineResponse toDto(Line line);
}
