package fpt.qn.mes.master.machine.application.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import fpt.qn.mes.master.machine.application.dto.request.UpdateMachineRequest;
import fpt.qn.mes.master.machine.application.dto.response.MachineDto;
import fpt.qn.mes.master.machine.application.exception.MachineNotFoundException;
import fpt.qn.mes.master.machine.application.mapper.MachineDtoMapper;
import fpt.qn.mes.master.machine.domain.entities.Machine;
import fpt.qn.mes.master.machine.domain.repository.MachineRepository;

@ExtendWith(MockitoExtension.class)
class MachineServiceTest {

    @Mock MachineRepository machineRepository;
    @Mock MachineDtoMapper mapper;
    @InjectMocks MachineService machineService;

    UUID id = UUID.randomUUID();
    UUID lineId = UUID.randomUUID();
    UUID userId = UUID.randomUUID();
    UUID statusId = UUID.randomUUID();
    Machine machine;
    MachineDto dto;

    @BeforeEach
    void setUp() {
        machine = Machine.create(lineId, "MC-TEST", "Test MC", statusId, userId);
        dto = MachineDto.builder().id(id).code("MC-TEST").build();
    }

    @Test
    void getMachineById_NotFound_Throws404() {
        when(machineRepository.findById(id)).thenReturn(Optional.empty());
        assertThrows(MachineNotFoundException.class, () -> machineService.getMachineById(id));
    }

    @Test
    void getMachineById_Success() {
        when(machineRepository.findById(id)).thenReturn(Optional.of(machine));
        when(mapper.toDto(machine)).thenReturn(dto);
        var result = machineService.getMachineById(id);
        assertNotNull(result);
    }

    @Test
    void updateMachine_Success() {
        when(machineRepository.findById(id)).thenReturn(Optional.of(machine));
        when(machineRepository.update(any())).thenReturn(machine);
        when(mapper.toDto(any())).thenReturn(dto);
        var req = new UpdateMachineRequest();
        req.setName("Updated");
        var result = machineService.updateMachine(id, req, userId);
        assertNotNull(result);
    }
}
