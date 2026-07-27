package fpt.qn.mes.master.warehouse.application.mapper;

import org.mapstruct.Mapper;

import fpt.qn.mes.master.warehouse.application.dto.response.WarehouseDto;
import fpt.qn.mes.master.warehouse.domain.entities.Warehouse;

@Mapper(componentModel = "spring")
public interface WarehouseDtoMapper {

    WarehouseDto toDto(Warehouse warehouse);
}
