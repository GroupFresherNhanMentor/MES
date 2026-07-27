package fpt.qn.mes.user.application.mapper;

import org.mapstruct.Mapper;

import fpt.qn.mes.user.application.dto.response.UserDto;
import fpt.qn.mes.user.domain.entities.User;

@Mapper(componentModel = "spring")
public interface UserDtoMapper {

    UserDto toDto(User user);
}
