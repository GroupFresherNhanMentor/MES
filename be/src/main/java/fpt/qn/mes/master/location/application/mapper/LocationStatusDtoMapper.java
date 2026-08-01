package fpt.qn.mes.master.location.application.mapper;

import org.mapstruct.Mapper;
import fpt.qn.mes.master.location.application.dto.locationstatus.LocationStatusResponse;
import fpt.qn.mes.master.location.domain.entities.LocationStatus;

@Mapper(componentModel = "spring")
public interface LocationStatusDtoMapper {
    LocationStatusResponse toDto(LocationStatus status);
}
