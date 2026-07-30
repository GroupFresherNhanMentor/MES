package fpt.qn.mes.user.presentation;

import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import fpt.qn.mes.common.dto.response.ApiResponse;
import fpt.qn.mes.common.dto.response.PageResponse;
import fpt.qn.mes.auth.application.security.RoleAccess;
import fpt.qn.mes.user.application.dto.request.CreateUserRequest;
import fpt.qn.mes.user.application.dto.request.UpdateUserRequest;
import fpt.qn.mes.user.application.dto.response.UserDto;
import fpt.qn.mes.user.application.port.in.UserUseCase;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UserController {

    UserUseCase userUseCase;

    @GetMapping
    @PreAuthorize(RoleAccess.ADMIN_ONLY)
    public ResponseEntity<ApiResponse<PageResponse<UserDto>>> getUsers(
            @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(ApiResponse.success(userUseCase.getUsers(page, size), "Users retrieved"));
    }

    @GetMapping("/{id}")
    @PreAuthorize(RoleAccess.ADMIN_ONLY)
    public ResponseEntity<ApiResponse<UserDto>> getUserById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(userUseCase.getUserById(id), "User retrieved"));
    }

    @PostMapping
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201")
    @PreAuthorize(RoleAccess.ADMIN_ONLY)
    public ResponseEntity<ApiResponse<UserDto>> createUser(@Valid @RequestBody CreateUserRequest request) {
        return ResponseEntity.status(org.springframework.http.HttpStatus.CREATED)
                .body(ApiResponse.success(userUseCase.createUser(request), "User created"));
    }

    @PutMapping("/{id}")
    @PreAuthorize(RoleAccess.ADMIN_ONLY)
    public ResponseEntity<ApiResponse<UserDto>> updateUser(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateUserRequest request) {
        return ResponseEntity.ok(ApiResponse.success(userUseCase.updateUser(id, request), "User updated"));
    }

    @PatchMapping("/{id}/activate")
    @PreAuthorize(RoleAccess.ADMIN_ONLY)
    public ResponseEntity<ApiResponse<Void>> activateUser(@PathVariable UUID id) {
        userUseCase.activateUser(id);
        return ResponseEntity.ok(ApiResponse.success(null, "User activated"));
    }

    @PatchMapping("/{id}/deactivate")
    @PreAuthorize(RoleAccess.ADMIN_ONLY)
    public ResponseEntity<ApiResponse<Void>> deactivateUser(@PathVariable UUID id) {
        userUseCase.deactivateUser(id);
        return ResponseEntity.ok(ApiResponse.success(null, "User deactivated"));
    }
}
