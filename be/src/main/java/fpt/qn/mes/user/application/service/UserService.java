package fpt.qn.mes.user.application.service;

import java.util.UUID;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import fpt.qn.mes.audit.domain.entities.AuditAction;
import fpt.qn.mes.audit.domain.events.AuditEvent;
import fpt.qn.mes.auth.application.port.out.CurrentUserPort;
import fpt.qn.mes.common.dto.response.PageResponse;
import fpt.qn.mes.common.port.out.JsonSerializerPort;
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
    CurrentUserPort currentUserPort;
    ApplicationEventPublisher eventPublisher;
    JsonSerializerPort jsonSerializer;

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
        User saved = userRepository.save(user);
        UUID actorId = currentUserPort.getCurrentUserId();
        eventPublisher.publishEvent(AuditEvent.create(actorId, AuditAction.CREATE_USER,
                "USER", saved.getId(), null, jsonSerializer.toJson(saved), null));
        return userDtoMapper.toDto(saved);
    }

    @Override
    @Transactional
    public UserResponse updateUser(UUID id, UpdateUserRequest request) {
        administrativeAccessGuard.lock();
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User not found"));
        User updated = userRepository.update(user.updateFullName(request.getFullName()));
        UUID actorId = currentUserPort.getCurrentUserId();
        eventPublisher.publishEvent(AuditEvent.create(actorId, AuditAction.UPDATE_USER,
                "USER", id, jsonSerializer.toJson(user), jsonSerializer.toJson(updated), null));
        return userDtoMapper.toDto(updated);
    }

    @Override
    @Transactional
    public void activateUser(UUID id) {
        administrativeAccessGuard.lock();
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User not found"));
        user.activate();
        userRepository.update(user);
        UUID actorId = currentUserPort.getCurrentUserId();
        eventPublisher.publishEvent(AuditEvent.create(actorId, AuditAction.ACTIVATE_USER,
                "USER", id, null, jsonSerializer.toJson(user), null));
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
        UUID actorId = currentUserPort.getCurrentUserId();
        eventPublisher.publishEvent(AuditEvent.create(actorId, AuditAction.DEACTIVATE_USER,
                "USER", id, null, jsonSerializer.toJson(user), null));
    }
}
