package fpt.qn.mes.master.machine.presentation;

import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import fpt.qn.mes.auth.application.security.AppUserPrincipal;
import fpt.qn.mes.common.dto.response.ApiResponse;
import fpt.qn.mes.common.dto.response.PageResponse;
import fpt.qn.mes.master.machine.application.dto.request.ChangeMachineStatusRequest;
import fpt.qn.mes.master.machine.application.dto.request.CreateMachineRequest;
import fpt.qn.mes.master.machine.application.dto.request.UpdateMachineRequest;
import fpt.qn.mes.master.machine.application.dto.response.MachineDto;
import fpt.qn.mes.master.machine.application.port.in.MachineUseCase;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@RestController
@RequestMapping("/api/machines")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class MachineController {

    MachineUseCase machineUseCase;

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<MachineDto>>> getMachines(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) UUID statusId) {
        var result = (statusId != null)
                ? machineUseCase.getMachinesByStatus(page, size, statusId)
                : machineUseCase.getMachines(page, size);
        return ResponseEntity.ok(ApiResponse.success(result, "OK"));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<MachineDto>> getMachineById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(machineUseCase.getMachineById(id), "OK"));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<MachineDto>> createMachine(
            @Valid @RequestBody CreateMachineRequest request,
            @AuthenticationPrincipal AppUserPrincipal principal) {
        var result = machineUseCase.createMachine(request, principal.getId());
        return ResponseEntity.status(201).body(ApiResponse.success(result, "Created"));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<MachineDto>> updateMachine(
            @PathVariable UUID id, @RequestBody UpdateMachineRequest request,
            @AuthenticationPrincipal AppUserPrincipal principal) {
        var result = machineUseCase.updateMachine(id, request, principal.getId());
        return ResponseEntity.ok(ApiResponse.success(result, "Updated"));
    }

    @PutMapping("/{id}/deactivate")
    public ResponseEntity<ApiResponse<Void>> deactivateMachine(@PathVariable UUID id) {
        machineUseCase.deleteMachine(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Deactivated"));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<ApiResponse<MachineDto>> changeMachineStatus(
            @PathVariable UUID id, @Valid @RequestBody ChangeMachineStatusRequest request,
            @AuthenticationPrincipal AppUserPrincipal principal) {
        var result = machineUseCase.changeMachineStatus(id, request.getStatusId(), principal.getId());
        return ResponseEntity.ok(ApiResponse.success(result, "Status updated"));
    }
}
