package fpt.qn.mes.role.application.mapper;

import org.mapstruct.Mapper;

import fpt.qn.mes.role.application.dto.response.PermissionResponse;
import fpt.qn.mes.role.application.dto.response.RoleResponse;
import fpt.qn.mes.role.domain.entities.Permission;
import fpt.qn.mes.role.domain.entities.Role;

@Mapper(componentModel = "spring")
public interface RoleDtoMapper {

    RoleResponse toDto(Role role);

    PermissionResponse toDto(Permission permission);
}
