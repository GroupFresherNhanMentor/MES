package fpt.qn.mes.master.line.application.service;

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
import fpt.qn.mes.master.line.application.dto.line.create.CreateLineRequest;
import fpt.qn.mes.master.line.application.dto.line.LineResponse;
import fpt.qn.mes.master.line.application.exception.LineConflictException;
import fpt.qn.mes.master.line.application.exception.LineStatusNotFoundException;
import fpt.qn.mes.master.line.application.exception.LineNotFoundException;
import fpt.qn.mes.master.line.application.mapper.LineDtoMapper;
import fpt.qn.mes.master.line.application.port.out.LineMachinePort;
import fpt.qn.mes.master.line.domain.constants.LineStatusConstants;
import fpt.qn.mes.master.line.domain.entities.Line;
import fpt.qn.mes.master.line.domain.entities.LineStatus;
import fpt.qn.mes.master.line.domain.repository.LineRepository;
import fpt.qn.mes.master.line.domain.repository.LineStatusRepository;

@ExtendWith(MockitoExtension.class)
class LineServiceTest {

    @Mock LineRepository lineRepository;
    @Mock LineStatusRepository lineStatusRepository;
    @Mock LineMachinePort lineMachinePort;
    @Mock LineDtoMapper mapper;
    @Mock CurrentUserPort currentUserPort;
    @Mock ApplicationEventPublisher eventPublisher;
    @Mock JsonSerializerPort jsonSerializer;
    @InjectMocks LineService lineService;

    UUID id = UUID.randomUUID();
    UUID userId = UUID.randomUUID();
    UUID statusId = UUID.randomUUID();
    Line line;
    LineResponse response;

    @BeforeEach
    void setUp() {
        line = Line.create("LN-TEST", "Test Line", statusId, userId);
        response = LineResponse.builder().id(id).code("LN-TEST").build();
    }

    @Test
    void createLine_Success() {
        LineStatus activeStatus = LineStatus.builder().id(statusId).name(LineStatusConstants.ACTIVE).build();
        when(lineRepository.existsByCode("LN-TEST")).thenReturn(false);
        when(lineStatusRepository.findByName(LineStatusConstants.ACTIVE)).thenReturn(Optional.of(activeStatus));
        when(currentUserPort.getCurrentUserId()).thenReturn(userId);
        when(lineRepository.save(any(Line.class))).thenReturn(line);

        var req = new CreateLineRequest();
        req.setCode("LN-TEST");
        req.setName("Test");

        assertDoesNotThrow(() -> lineService.createLine(req));
        verify(lineRepository).save(any(Line.class));
    }

    @Test
    void createLine_DuplicateCode_ThrowsConflict() {
        when(lineRepository.existsByCode("LN-TEST")).thenReturn(true);
        var req = new CreateLineRequest();
        req.setCode("LN-TEST");
        req.setName("Test");
        assertThrows(LineConflictException.class, () -> lineService.createLine(req));
    }

    @Test
    void createLine_ActiveStatusNotFound_ThrowsStatusNotFoundException() {
        when(lineRepository.existsByCode("LN-TEST")).thenReturn(false);
        when(lineStatusRepository.findByName(LineStatusConstants.ACTIVE)).thenReturn(Optional.empty());
        var req = new CreateLineRequest();
        req.setCode("LN-TEST");
        req.setName("Test");
        assertThrows(LineStatusNotFoundException.class, () -> lineService.createLine(req));
    }

    @Test
    void getLineById_NotFound_Throws404() {
        when(lineRepository.findById(id)).thenReturn(Optional.empty());
        assertThrows(LineNotFoundException.class, () -> lineService.getLineById(id));
    }

    @Test
    void getLineById_Success() {
        when(lineRepository.findById(id)).thenReturn(Optional.of(line));
        when(mapper.toDto(line)).thenReturn(response);
        var result = lineService.getLineById(id);
        assertNotNull(result);
    }

    @Test
    void deactivateLine_WithRunningMachines_ThrowsConflict() {
        when(lineRepository.findById(id)).thenReturn(Optional.of(line));
        when(lineMachinePort.hasRunningMachines(id)).thenReturn(true);
        assertThrows(LineConflictException.class, () -> lineService.deactivateLine(id));
        verify(lineStatusRepository, never()).findByName(any());
    }

    @Test
    void deactivateLine_NoRunningMachines_Succeeds() {
        UUID inactiveStatusId = UUID.randomUUID();
        LineStatus inactiveStatus = LineStatus.builder().id(inactiveStatusId).name(LineStatusConstants.INACTIVE).build();
        when(lineRepository.findById(id)).thenReturn(Optional.of(line));
        when(lineMachinePort.hasRunningMachines(id)).thenReturn(false);
        when(lineStatusRepository.findByName(LineStatusConstants.INACTIVE)).thenReturn(Optional.of(inactiveStatus));
        when(currentUserPort.getCurrentUserId()).thenReturn(userId);
        when(lineRepository.update(any(Line.class))).thenReturn(line);
        assertDoesNotThrow(() -> lineService.deactivateLine(id));
        verify(lineRepository).update(any(Line.class));
    }
}
