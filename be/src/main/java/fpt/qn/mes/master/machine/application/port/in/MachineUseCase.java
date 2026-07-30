package fpt.qn.mes.master.machine.application.port.in;

import java.util.UUID;

import fpt.qn.mes.common.dto.response.PageResponse;
import fpt.qn.mes.master.machine.application.dto.request.CreateMachineRequest;
import fpt.qn.mes.master.machine.application.dto.response.MachineDto;
import fpt.qn.mes.master.machine.application.dto.request.UpdateMachineRequest;

public interface MachineUseCase {
    PageResponse<MachineDto> getMachines(int page, int size);
    PageResponse<MachineDto> getMachinesByStatus(int page, int size, UUID statusId);
    MachineDto getMachineById(UUID id);
    boolean isAvailableForReservation(UUID id);
    MachineDto createMachine(CreateMachineRequest request, UUID currentUserId);
    MachineDto updateMachine(UUID id, UpdateMachineRequest request, UUID currentUserId);
    void deleteMachine(UUID id);
    MachineDto changeMachineStatus(UUID id, UUID newStatusId, UUID currentUserId);
}
