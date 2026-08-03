package fpt.qn.mes.inventory.presentation;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import fpt.qn.mes.common.dto.response.ApiResponse;
import fpt.qn.mes.common.dto.response.PageResponse;
import fpt.qn.mes.inventory.application.dto.movementtype.MovementTypeResponse;
import fpt.qn.mes.inventory.application.dto.movementtype.create.CreateMovementTypeRequest;
import fpt.qn.mes.inventory.application.dto.movementtype.search.MovementTypeSearchRequest;
import fpt.qn.mes.inventory.application.port.in.MovementTypeUseCase;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@RestController
@RequestMapping("/api/movement-types")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class MovementTypeController {

    MovementTypeUseCase movementTypeUseCase;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<PageResponse<MovementTypeResponse>>> getMovementTypes(
            @ModelAttribute MovementTypeSearchRequest request) {
        return ResponseEntity.ok(ApiResponse.success(movementTypeUseCase.getMovementTypes(request), "OK"));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> createMovementType(
            @Valid @RequestBody CreateMovementTypeRequest request) {
        movementTypeUseCase.createMovementType(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Created"));
    }
}
