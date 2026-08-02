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

import org.springframework.context.ApplicationEventPublisher;
import fpt.qn.mes.auth.application.port.out.CurrentUserPort;
import fpt.qn.mes.common.port.out.JsonSerializerPort;
import fpt.qn.mes.master.machine.application.dto.machine.MachineResponse;
import fpt.qn.mes.master.machine.application.dto.machine.update.UpdateMachineRequest;
import fpt.qn.mes.master.machine.application.exception.MachineNotFoundException;
import fpt.qn.mes.master.machine.application.mapper.MachineDtoMapper;
import fpt.qn.mes.master.machine.domain.entities.Machine;
import fpt.qn.mes.master.machine.domain.repository.MachineRepository;

@ExtendWith(MockitoExtension.class)
class MachineServiceTest {

    @Mock MachineRepository machineRepository;
    @Mock MachineDtoMapper mapper;
    @Mock CurrentUserPort currentUserPort;
    @Mock ApplicationEventPublisher eventPublisher;
    @Mock JsonSerializerPort jsonSerializer;
    @InjectMocks MachineService machineService;

    UUID id = UUID.randomUUID();
    UUID lineId = UUID.randomUUID();
    UUID userId = UUID.randomUUID();
    UUID statusId = UUID.randomUUID();
    Machine machine;
    MachineResponse response;

    @BeforeEach
    void setUp() {
        machine = Machine.create(lineId, "MC-TEST", "Test MC", statusId, userId);
        response = MachineResponse.builder().id(id).code("MC-TEST").build();
    }

    @Test
    void getMachineById_NotFound_Throws404() {
        when(machineRepository.findById(id)).thenReturn(Optional.empty());
        assertThrows(MachineNotFoundException.class, () -> machineService.getMachineById(id));
    }

    @Test
    void getMachineById_Success() {
        when(machineRepository.findById(id)).thenReturn(Optional.of(machine));
        when(mapper.toDto(machine)).thenReturn(response);
        var result = machineService.getMachineById(id);
        assertNotNull(result);
    }

    @Test
    void updateMachine_Success() {
        when(currentUserPort.getCurrentUserId()).thenReturn(userId);
        when(machineRepository.findById(id)).thenReturn(Optional.of(machine));
        when(machineRepository.update(any())).thenReturn(machine);
        var req = new UpdateMachineRequest();
        req.setName("Updated");
        assertDoesNotThrow(() -> machineService.updateMachine(id, req));
        verify(machineRepository).update(any());
    }
}
