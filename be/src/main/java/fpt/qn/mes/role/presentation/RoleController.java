package fpt.qn.mes.role.presentation;

import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import fpt.qn.mes.common.dto.response.ApiResponse;
import fpt.qn.mes.role.application.dto.request.AssignPermissionsRequest;
import fpt.qn.mes.role.application.dto.request.CreateRoleRequest;
import fpt.qn.mes.role.application.dto.response.RoleDto;
import fpt.qn.mes.role.application.dto.request.UpdateRoleRequest;
import fpt.qn.mes.role.application.port.in.RoleUseCase;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@RestController
@RequestMapping("/api/roles")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class RoleController {

    RoleUseCase roleUseCase;

    @GetMapping
    public ResponseEntity<ApiResponse<List<RoleDto>>> getRoles() {
        throw new UnsupportedOperationException("Not implemented");
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<RoleDto>> getRoleById(@PathVariable UUID id) {
        throw new UnsupportedOperationException("Not implemented");
    }

    @PostMapping
    public ResponseEntity<ApiResponse<RoleDto>> createRole(@Valid @RequestBody CreateRoleRequest request) {
        throw new UnsupportedOperationException("Not implemented");
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<RoleDto>> updateRole(@PathVariable UUID id, @RequestBody UpdateRoleRequest request) {
        throw new UnsupportedOperationException("Not implemented");
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteRole(@PathVariable UUID id) {
        throw new UnsupportedOperationException("Not implemented");
    }

    @PostMapping("/{id}/permissions")
    public ResponseEntity<ApiResponse<Void>> assignPermissions(@PathVariable UUID id, @RequestBody AssignPermissionsRequest request) {
        throw new UnsupportedOperationException("Not implemented");
    }
}
