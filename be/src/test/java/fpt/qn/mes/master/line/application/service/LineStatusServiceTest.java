package fpt.qn.mes.master.line.application.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import fpt.qn.mes.common.domainQuery.PaginationResult;
import fpt.qn.mes.master.line.application.dto.linestatus.LineStatusResponse;
import fpt.qn.mes.master.line.application.dto.linestatus.create.CreateLineStatusRequest;
import fpt.qn.mes.master.line.application.dto.linestatus.search.LineStatusSearchRequest;
import fpt.qn.mes.master.line.application.exception.LineStatusConflictException;
import fpt.qn.mes.master.line.application.mapper.LineStatusDtoMapper;
import fpt.qn.mes.master.line.domain.entities.LineStatus;
import fpt.qn.mes.master.line.domain.repository.LineStatusRepository;

@ExtendWith(MockitoExtension.class)
class LineStatusServiceTest {

    @Mock LineStatusRepository linestatusRepository;
    @Mock LineStatusDtoMapper mapper;
    @InjectMocks LineStatusService linestatusService;

    @Test
    void getLineStatuss_Success() {
        LineStatus status = LineStatus.builder().id(UUID.randomUUID()).name("ACTIVE").build();
        LineStatusResponse response = LineStatusResponse.builder().id(status.getId()).name("ACTIVE").build();
        PaginationResult<LineStatus> page = PaginationResult.<LineStatus>builder().total(1).items(List.of(status)).build();

        when(linestatusRepository.search(any())).thenReturn(page);
        when(mapper.toDto(status)).thenReturn(response);

        var result = linestatusService.getLineStatuses(new LineStatusSearchRequest());

        assertEquals(1, result.getItems().size());
        assertEquals("ACTIVE", result.getItems().get(0).getName());
    }

    @Test
    void createLineStatus_Success() {
        when(linestatusRepository.existsByName("ACTIVE")).thenReturn(false);
        var req = new CreateLineStatusRequest();
        req.setName("ACTIVE");
        req.setDescription("Desc");

        assertDoesNotThrow(() -> linestatusService.createLineStatus(req));
        verify(linestatusRepository).save(any());
    }

    @Test
    void createLineStatus_Duplicate_ThrowsConflict() {
        when(linestatusRepository.existsByName("ACTIVE")).thenReturn(true);
        var req = new CreateLineStatusRequest();
        req.setName("ACTIVE");

        assertThrows(LineStatusConflictException.class, () -> linestatusService.createLineStatus(req));
    }
}
