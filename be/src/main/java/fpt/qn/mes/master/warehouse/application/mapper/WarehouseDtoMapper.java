package fpt.qn.mes.master.warehouse.application.mapper;

import org.mapstruct.Mapper;
import fpt.qn.mes.master.warehouse.application.dto.warehouse.WarehouseResponse;
import fpt.qn.mes.master.warehouse.domain.entities.Warehouse;

@Mapper(componentModel = "spring", uses = {WarehouseStatusDtoMapper.class})
public interface WarehouseDtoMapper {
    WarehouseResponse toDto(Warehouse warehouse);
}
