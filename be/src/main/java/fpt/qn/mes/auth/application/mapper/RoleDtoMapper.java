package fpt.qn.mes.auth.application.mapper;

import org.mapstruct.Mapper;

import fpt.qn.mes.auth.application.dto.response.RoleDto;
import fpt.qn.mes.auth.domain.entities.Role;

@Mapper(componentModel = "spring")
public interface RoleDtoMapper {

    RoleDto toDto(Role role);

}
