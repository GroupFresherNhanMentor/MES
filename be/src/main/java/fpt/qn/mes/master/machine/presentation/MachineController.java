package fpt.qn.mes.master.machine.presentation;

import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import fpt.qn.mes.common.dto.response.ApiResponse;
import fpt.qn.mes.common.dto.response.PageResponse;
import fpt.qn.mes.master.machine.application.dto.machine.MachineResponse;
import fpt.qn.mes.master.machine.application.dto.machine.create.CreateMachineRequest;
import fpt.qn.mes.master.machine.application.dto.machine.search.MachineSearchRequest;
import fpt.qn.mes.master.machine.application.dto.machine.update.ChangeMachineStatusRequest;
import fpt.qn.mes.master.machine.application.dto.machine.update.UpdateMachineRequest;
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
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERATOR', 'PLANNER', 'FACTORY_MANAGER', 'MAINTENANCE_ENGINEER', 'AUDITOR')")
    public ResponseEntity<ApiResponse<PageResponse<MachineResponse>>> getMachines(
            @ModelAttribute MachineSearchRequest request) {
        return ResponseEntity.ok(ApiResponse.success(machineUseCase.getMachines(request), "OK"));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERATOR', 'PLANNER', 'FACTORY_MANAGER', 'MAINTENANCE_ENGINEER', 'AUDITOR')")
    public ResponseEntity<ApiResponse<MachineResponse>> getMachineById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(machineUseCase.getMachineById(id), "OK"));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> createMachine(@Valid @RequestBody CreateMachineRequest request) {
        machineUseCase.createMachine(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Created"));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> updateMachine(
            @PathVariable UUID id, @Valid @RequestBody UpdateMachineRequest request) {
        machineUseCase.updateMachine(id, request);
        return ResponseEntity.ok(ApiResponse.success("Updated"));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('ADMIN', 'MAINTENANCE_ENGINEER')")
    public ResponseEntity<ApiResponse<Void>> changeMachineStatus(
            @PathVariable UUID id, @Valid @RequestBody ChangeMachineStatusRequest request) {
        machineUseCase.changeMachineStatus(id, request.getStatusId());
        return ResponseEntity.ok(ApiResponse.success("Status updated"));
    }
}
