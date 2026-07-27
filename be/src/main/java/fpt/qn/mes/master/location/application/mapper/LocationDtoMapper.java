package fpt.qn.mes.master.location.application.mapper;

import org.mapstruct.Mapper;

import fpt.qn.mes.master.location.application.dto.response.WarehouseLocationDto;
import fpt.qn.mes.master.location.domain.entities.WarehouseLocation;

@Mapper(componentModel = "spring")
public interface LocationDtoMapper {

    WarehouseLocationDto toDto(WarehouseLocation warehouseLocation);
}
