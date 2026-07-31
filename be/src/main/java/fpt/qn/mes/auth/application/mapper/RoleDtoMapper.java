package fpt.qn.mes.auth.application.mapper;

import org.mapstruct.Mapper;

import fpt.qn.mes.auth.application.dto.response.RoleResponse;
import fpt.qn.mes.auth.domain.entities.Role;

@Mapper(componentModel = "spring")
public interface RoleDtoMapper {

    RoleResponse toDto(Role role);
}
