package fpt.qn.mes.user.application.service;

import java.util.UUID;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import fpt.qn.mes.common.dto.PageResponse;
import fpt.qn.mes.user.application.dto.CreateUserRequest;
import fpt.qn.mes.user.application.dto.UpdateUserRequest;
import fpt.qn.mes.user.application.dto.UserDto;
import fpt.qn.mes.user.application.mapper.UserDtoMapper;
import fpt.qn.mes.user.application.port.in.UserUseCase;
import fpt.qn.mes.user.domain.repository.UserRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UserService implements UserUseCase {

    UserRepository userRepository;
    PasswordEncoder passwordEncoder;
    UserDtoMapper userDtoMapper;

    @Override @Transactional(readOnly = true)
    public PageResponse<UserDto> getUsers(int page, int size) {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override @Transactional(readOnly = true)
    public UserDto getUserById(UUID id) {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override @Transactional
    public UserDto createUser(CreateUserRequest request) {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override @Transactional
    public UserDto updateUser(UUID id, UpdateUserRequest request) {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override @Transactional
    public void activateUser(UUID id) {}

    @Override @Transactional
    public void deactivateUser(UUID id) {}
}
