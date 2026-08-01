package fpt.qn.mes.master.warehouse.application.mapper;

import org.mapstruct.Mapper;
import fpt.qn.mes.master.warehouse.application.dto.warehousestatus.WarehouseStatusResponse;
import fpt.qn.mes.master.warehouse.domain.entities.WarehouseStatus;

@Mapper(componentModel = "spring")
public interface WarehouseStatusDtoMapper {
    WarehouseStatusResponse toDto(WarehouseStatus status);
}
