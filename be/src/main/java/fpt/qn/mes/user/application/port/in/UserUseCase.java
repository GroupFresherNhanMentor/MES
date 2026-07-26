package fpt.qn.mes.user.application.port.in;

import java.util.UUID;

import fpt.qn.mes.common.dto.PageResponse;
import fpt.qn.mes.user.application.dto.CreateUserRequest;
import fpt.qn.mes.user.application.dto.UpdateUserRequest;
import fpt.qn.mes.user.application.dto.UserDto;

public interface UserUseCase {

    PageResponse<UserDto> getUsers(int page, int size);

    UserDto getUserById(UUID id);

    UserDto createUser(CreateUserRequest request);

    UserDto updateUser(UUID id, UpdateUserRequest request);

    void activateUser(UUID id);

    void deactivateUser(UUID id);
}
