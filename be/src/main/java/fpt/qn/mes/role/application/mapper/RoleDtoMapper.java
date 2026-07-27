package fpt.qn.mes.role.application.mapper;

import org.mapstruct.Mapper;

import fpt.qn.mes.role.application.dto.response.PermissionDto;
import fpt.qn.mes.role.application.dto.response.RoleDto;
import fpt.qn.mes.role.domain.entities.Permission;
import fpt.qn.mes.role.domain.entities.Role;

@Mapper(componentModel = "spring")
public interface RoleDtoMapper {

    RoleDto toDto(Role role);

    PermissionDto toDto(Permission permission);
}
