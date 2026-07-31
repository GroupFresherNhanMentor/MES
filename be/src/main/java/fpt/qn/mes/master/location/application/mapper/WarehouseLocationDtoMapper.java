package fpt.qn.mes.master.location.application.mapper;

import org.mapstruct.Mapper;
import fpt.qn.mes.master.location.application.dto.warehouselocation.WarehouseLocationResponse;
import fpt.qn.mes.master.location.domain.entities.WarehouseLocation;

@Mapper(componentModel = "spring", uses = {LocationStatusDtoMapper.class})
public interface WarehouseLocationDtoMapper {
    WarehouseLocationResponse toDto(WarehouseLocation location);
}
