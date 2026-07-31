package fpt.qn.mes.master.machine.presentation;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import fpt.qn.mes.common.dto.response.ApiResponse;
import fpt.qn.mes.common.dto.response.PageResponse;
import fpt.qn.mes.master.machine.application.dto.machinestatus.MachineStatusResponse;
import fpt.qn.mes.master.machine.application.dto.machinestatus.create.CreateMachineStatusRequest;
import fpt.qn.mes.master.machine.application.dto.machinestatus.search.MachineStatusSearchRequest;
import fpt.qn.mes.master.machine.application.port.in.MachineStatusUseCase;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@RestController
@RequestMapping("/api/machine-statuses")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class MachineStatusController {

    MachineStatusUseCase machineStatusUseCase;

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<MachineStatusResponse>>> getMachineStatuses(
            @ModelAttribute MachineStatusSearchRequest request) {
        return ResponseEntity.ok(ApiResponse.success(machineStatusUseCase.getMachineStatuses(request), "OK"));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Void>> createMachineStatus(
            @Valid @RequestBody CreateMachineStatusRequest request) {
        machineStatusUseCase.createMachineStatus(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Created"));
    }
}
