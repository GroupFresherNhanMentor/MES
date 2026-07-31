package fpt.qn.mes.inventory.application.mapper;

import org.mapstruct.Mapper;

import fpt.qn.mes.inventory.application.dto.movementtype.MovementTypeResponse;
import fpt.qn.mes.inventory.domain.entities.MovementType;

@Mapper(componentModel = "spring")
public interface MovementTypeDtoMapper {
    MovementTypeResponse toDto(MovementType entity);
}
