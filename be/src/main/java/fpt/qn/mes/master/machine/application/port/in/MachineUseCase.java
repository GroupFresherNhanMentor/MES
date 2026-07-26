package fpt.qn.mes.master.machine.application.port.in;

import java.util.UUID;

import fpt.qn.mes.common.dto.PageResponse;
import fpt.qn.mes.master.machine.application.dto.CreateMachineRequest;
import fpt.qn.mes.master.machine.application.dto.MachineDto;
import fpt.qn.mes.master.machine.application.dto.UpdateMachineRequest;

public interface MachineUseCase {
    PageResponse<MachineDto> getMachines(int page, int size);
    MachineDto getMachineById(UUID id);
    MachineDto createMachine(CreateMachineRequest request, UUID currentUserId);
    MachineDto updateMachine(UUID id, UpdateMachineRequest request, UUID currentUserId);
    void deleteMachine(UUID id);
}
