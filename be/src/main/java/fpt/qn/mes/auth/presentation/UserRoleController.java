package fpt.qn.mes.auth.presentation;

import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import fpt.qn.mes.auth.application.dto.request.ReplaceUserRolesRequest;
import fpt.qn.mes.auth.application.dto.response.RoleDto;
import fpt.qn.mes.auth.application.port.in.UserRoleUseCase;
import fpt.qn.mes.auth.application.security.RoleAccess;
import fpt.qn.mes.common.dto.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@RestController
@RequestMapping("/api/users/{userId}/roles")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UserRoleController {

    UserRoleUseCase userRoleUseCase;

    @GetMapping
    @PreAuthorize(RoleAccess.ADMIN_ONLY)
    public ResponseEntity<ApiResponse<List<RoleDto>>> getUserRoles(@PathVariable UUID userId) {
        return ResponseEntity.ok(ApiResponse.success(
                userRoleUseCase.getUserRoles(userId), "User roles retrieved"));
    }

    @PutMapping
    @PreAuthorize(RoleAccess.ADMIN_ONLY)
    public ResponseEntity<ApiResponse<List<RoleDto>>> replaceUserRoles(
            @PathVariable UUID userId,
            @Valid @RequestBody ReplaceUserRolesRequest request) {
        return ResponseEntity.ok(ApiResponse.success(
                userRoleUseCase.replaceUserRoles(userId, request), "User roles replaced"));
    }
}
