package fpt.qn.mes.user.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import fpt.qn.mes.auth.application.port.out.PasswordPort;
import fpt.qn.mes.auth.application.port.in.AdministrativeAccessGuardUseCase;
import fpt.qn.mes.common.domainQuery.PaginationResult;
import fpt.qn.mes.common.exception.ConflictException;
import fpt.qn.mes.user.application.dto.request.CreateUserRequest;
import fpt.qn.mes.user.application.dto.request.UpdateUserRequest;
import fpt.qn.mes.user.application.dto.response.UserResponse;
import fpt.qn.mes.user.application.exception.UserNotFoundException;
import fpt.qn.mes.user.application.mapper.UserDtoMapper;
import fpt.qn.mes.user.domain.entities.User;
import fpt.qn.mes.user.domain.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    UserRepository repository;

    @Mock
    PasswordPort passwordPort;

    @Mock
    UserDtoMapper mapper;

    @Mock
    AdministrativeAccessGuardUseCase guard;

    UserService service;

    @BeforeEach
    void setUp() {
        service = new UserService(repository, passwordPort, mapper, guard);
    }



    @Test
    void paginatedReadClampsBoundsAndDoesNotExposePassword() {
        User user = user(true);
        UserResponse dto = dto(user);
        when(repository.findAll(0, 100))
                .thenReturn(PaginationResult.<User>builder()
                        .items(List.of(user))
                        .total(1)
                        .build());
        when(mapper.toDto(user)).thenReturn(dto);

        var result = service.getUsers(-5, 500);

        assertThat(result.getItems()).containsExactly(dto);
        assertThat(result.getPageNumber()).isZero();
        assertThat(result.getPageSize()).isEqualTo(100);
        assertThat(UserResponse.class.getDeclaredFields())
                .extracting(field -> field.getName())
                .doesNotContain("password", "passwordHash");
    }

    @Test
    void createEncodesPasswordBeforePersistence() {
        CreateUserRequest request = new CreateUserRequest();
        request.setUsername(" alice ");
        request.setPassword("Password@123");
        request.setFullName("Alice");
        when(repository.existsByUsername(" alice ")).thenReturn(false);
        when(passwordPort.encode("Password@123")).thenReturn("$2a$encoded");
        when(repository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(mapper.toDto(any(User.class))).thenAnswer(invocation -> dto(invocation.getArgument(0)));

        service.createUser(request);

        ArgumentCaptor<User> saved = ArgumentCaptor.forClass(User.class);
        verify(repository).save(saved.capture());
        assertThat(saved.getValue().getUsername()).isEqualTo("alice");
        assertThat(saved.getValue().getPasswordHash()).isEqualTo("$2a$encoded");
        assertThat(saved.getValue().getActive()).isTrue();
    }

    @Test
    void duplicateUsernameReturnsConflictWithoutEncoding() {
        CreateUserRequest request = new CreateUserRequest();
        request.setUsername("alice");
        request.setPassword("Password@123");
        when(repository.existsByUsername("alice")).thenReturn(true);

        assertThatThrownBy(() -> service.createUser(request))
                .isInstanceOf(ConflictException.class);
        verify(passwordPort, never()).encode(any());
        verify(repository, never()).save(any());
    }

    @Test
    void missingUserReturnsConsistentNotFound() {
        UUID id = UUID.randomUUID();
        when(repository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getUserById(id))
                .isInstanceOf(UserNotFoundException.class);
    }

    @Test
    void deactivationLocksAndProtectsFinalAdministrator() {
        User user = user(true);
        when(repository.findById(user.getId())).thenReturn(Optional.of(user));
        when(repository.update(user)).thenReturn(user);

        service.deactivateUser(user.getId());

        verify(guard).lock();
        verify(repository).update(user);
        verify(guard).assertAdministrativeAccessRemains();
        assertThat(user.getActive()).isFalse();
    }


    @Test
    void updateAndActivatePreserveIdentityAndPassword() {
        User inactive = user(false);
        UpdateUserRequest request = new UpdateUserRequest();
        request.setFullName("Updated Alice");
        when(repository.findById(inactive.getId())).thenReturn(Optional.of(inactive));
        when(repository.update(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(mapper.toDto(any(User.class))).thenAnswer(invocation -> dto(invocation.getArgument(0)));

        UserResponse updated = service.updateUser(inactive.getId(), request);
        assertThat(updated.getFullName()).isEqualTo("Updated Alice");
        assertThat(updated.getUsername()).isEqualTo("alice");

        service.activateUser(inactive.getId());
        assertThat(inactive.getActive()).isTrue();
    }


    private User user(boolean active) {
        return User.builder()
                .id(UUID.randomUUID())
                .username("alice")
                .passwordHash("$2a$encoded")
                .fullName("Alice")
                .active(active)
                .createdAt(Instant.parse("2026-07-29T00:00:00Z"))
                .build();
    }

    private UserResponse dto(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .fullName(user.getFullName())
                .active(user.getActive())
                .createdAt(user.getCreatedAt())
                .build();
    }
}
