package fpt.qn.mes.master.machine.application.service;

import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import fpt.qn.mes.common.dto.PageResponse;
import fpt.qn.mes.master.machine.application.dto.CreateMachineRequest;
import fpt.qn.mes.master.machine.application.dto.MachineDto;
import fpt.qn.mes.master.machine.application.dto.UpdateMachineRequest;
import fpt.qn.mes.master.machine.application.mapper.MachineDtoMapper;
import fpt.qn.mes.master.machine.application.port.in.MachineUseCase;
import fpt.qn.mes.master.machine.domain.repository.MachineRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class MachineService implements MachineUseCase {

    MachineRepository machineRepository;
    MachineDtoMapper mapper;

    @Override @Transactional(readOnly = true)
    public PageResponse<MachineDto> getMachines(int page, int size) {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override @Transactional(readOnly = true)
    public MachineDto getMachineById(UUID id) {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override @Transactional
    public MachineDto createMachine(CreateMachineRequest request, UUID currentUserId) {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override @Transactional
    public MachineDto updateMachine(UUID id, UpdateMachineRequest request, UUID currentUserId) {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override @Transactional
    public void deleteMachine(UUID id) {}
}
