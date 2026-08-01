package fpt.qn.mes.user.application.service;

import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import fpt.qn.mes.common.dto.response.PageResponse;
import fpt.qn.mes.user.application.exception.UsernameAlreadyExistsException;
import fpt.qn.mes.auth.application.port.out.PasswordPort;
import fpt.qn.mes.auth.application.port.in.AdministrativeAccessGuardUseCase;
import fpt.qn.mes.user.application.dto.request.CreateUserRequest;
import fpt.qn.mes.user.application.dto.request.UpdateUserRequest;
import fpt.qn.mes.user.application.dto.response.UserResponse;
import fpt.qn.mes.user.application.mapper.UserDtoMapper;
import fpt.qn.mes.user.application.port.in.UserUseCase;
import fpt.qn.mes.user.application.exception.UserNotFoundException;
import fpt.qn.mes.user.domain.entities.User;
import fpt.qn.mes.user.domain.repository.UserRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UserService implements UserUseCase {

    UserRepository userRepository;
    PasswordPort passwordPort;
    UserDtoMapper userDtoMapper;
    AdministrativeAccessGuardUseCase administrativeAccessGuard;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<UserResponse> getUsers(int page, int size) {
        int normalizedPage = Math.max(0, page);
        int normalizedSize = Math.min(100, Math.max(1, size));
        var result = userRepository.findAll(normalizedPage, normalizedSize);
        java.util.List<UserResponse> items = new java.util.ArrayList<>();
        for (User user : result.getItems()) {
            items.add(userDtoMapper.toDto(user));
        }
        return PageResponse.<UserResponse>builder()
                .items(items)
                .totalElements(result.getTotal())
                .totalPages((int) Math.ceil((double) result.getTotal() / normalizedSize))
                .pageNumber(normalizedPage)
                .pageSize(normalizedSize)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse getUserById(UUID id) {
        return userRepository.findById(id)
                .map(user -> userDtoMapper.toDto(user))
                .orElseThrow(() -> new UserNotFoundException("User not found"));
    }

    @Override
    @Transactional
    public UserResponse createUser(CreateUserRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new UsernameAlreadyExistsException("Username already exists");
        }
        User user = User.create(
                request.getUsername(),
                passwordPort.encode(request.getPassword()),
                request.getFullName());
        return userDtoMapper.toDto(userRepository.save(user));
    }

    @Override
    @Transactional
    public UserResponse updateUser(UUID id, UpdateUserRequest request) {
        administrativeAccessGuard.lock();
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User not found"));
        return userDtoMapper.toDto(userRepository.update(user.updateFullName(request.getFullName())));
    }

    @Override
    @Transactional
    public void activateUser(UUID id) {
        administrativeAccessGuard.lock();
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User not found"));
        user.activate();
        userRepository.update(user);
    }

    @Override
    @Transactional
    public void deactivateUser(UUID id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User not found"));
        administrativeAccessGuard.lock();
        user.deactivate();
        userRepository.update(user);
        administrativeAccessGuard.assertAdministrativeAccessRemains();
    }
}
