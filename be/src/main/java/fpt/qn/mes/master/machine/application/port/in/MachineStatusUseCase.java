package fpt.qn.mes.master.machine.application.port.in;

import fpt.qn.mes.common.dto.response.PageResponse;
import fpt.qn.mes.master.machine.application.dto.machinestatus.MachineStatusResponse;
import fpt.qn.mes.master.machine.application.dto.machinestatus.create.CreateMachineStatusRequest;
import fpt.qn.mes.master.machine.application.dto.machinestatus.search.MachineStatusSearchRequest;

public interface MachineStatusUseCase {
    PageResponse<MachineStatusResponse> getMachineStatuses(MachineStatusSearchRequest request);
    void createMachineStatus(CreateMachineStatusRequest request);
}
