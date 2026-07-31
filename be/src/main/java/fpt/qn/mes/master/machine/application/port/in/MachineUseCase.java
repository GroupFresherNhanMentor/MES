package fpt.qn.mes.master.machine.application.port.in;

import java.util.UUID;

import fpt.qn.mes.common.dto.response.PageResponse;
import fpt.qn.mes.master.machine.application.dto.machine.MachineResponse;
import fpt.qn.mes.master.machine.application.dto.machine.create.CreateMachineRequest;
import fpt.qn.mes.master.machine.application.dto.machine.search.MachineSearchRequest;
import fpt.qn.mes.master.machine.application.dto.machine.update.UpdateMachineRequest;

public interface MachineUseCase {
    PageResponse<MachineResponse> getMachines(MachineSearchRequest request);
    MachineResponse getMachineById(UUID id);
    void createMachine(CreateMachineRequest request);
    void updateMachine(UUID id, UpdateMachineRequest request);
    void changeMachineStatus(UUID id, UUID newStatusId);
}
