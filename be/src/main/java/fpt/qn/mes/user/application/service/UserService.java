package fpt.qn.mes.user.application.service;

import java.util.UUID;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import fpt.qn.mes.common.dto.response.PageResponse;
import fpt.qn.mes.user.application.dto.request.CreateUserRequest;
import fpt.qn.mes.user.application.dto.request.UpdateUserRequest;
import fpt.qn.mes.user.application.dto.response.UserResponse;
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
    public PageResponse<UserResponse> getUsers(int page, int size) {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override @Transactional(readOnly = true)
    public UserResponse getUserById(UUID id) {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override @Transactional
    public UserResponse createUser(CreateUserRequest request) {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override @Transactional
    public UserResponse updateUser(UUID id, UpdateUserRequest request) {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override @Transactional
    public void activateUser(UUID id) {}

    @Override @Transactional
    public void deactivateUser(UUID id) {}
}
