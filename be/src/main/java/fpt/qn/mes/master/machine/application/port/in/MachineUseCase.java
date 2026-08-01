package fpt.qn.mes.master.machine.application.port.in;

import java.util.UUID;

import fpt.qn.mes.common.dto.response.PageResponse;
import fpt.qn.mes.master.machine.application.dto.machine.MachineResponse;
import fpt.qn.mes.master.machine.application.dto.machine.create.CreateMachineRequest;
import fpt.qn.mes.master.machine.application.dto.machine.search.MachineSearchRequest;
import fpt.qn.mes.master.machine.application.dto.machine.update.UpdateMachineRequest;

public interface MachineUseCase {
<<<<<<< HEAD
    PageResponse<MachineDto> getMachines(int page, int size);
    PageResponse<MachineDto> getMachinesByStatus(int page, int size, UUID statusId);
    MachineDto getMachineById(UUID id);
    boolean isAvailableForReservation(UUID id);
    MachineDto createMachine(CreateMachineRequest request, UUID currentUserId);
    MachineDto updateMachine(UUID id, UpdateMachineRequest request, UUID currentUserId);
    void deleteMachine(UUID id);
    MachineDto changeMachineStatus(UUID id, UUID newStatusId, UUID currentUserId);
=======
    PageResponse<MachineResponse> getMachines(MachineSearchRequest request);
    MachineResponse getMachineById(UUID id);
    boolean isAvailableForReservation(UUID id);
    void createMachine(CreateMachineRequest request);
    void updateMachine(UUID id, UpdateMachineRequest request);
    void changeMachineStatus(UUID id, UUID newStatusId);
>>>>>>> origin/develop
}
