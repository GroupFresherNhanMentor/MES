package fpt.qn.mes.master.line.application.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.Optional;
import java.util.UUID;

import org.jooq.DSLContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import fpt.qn.mes.common.dto.response.PaginationResult;
import fpt.qn.mes.master.line.application.dto.request.CreateLineRequest;
import fpt.qn.mes.master.line.application.dto.request.UpdateLineRequest;
import fpt.qn.mes.master.line.application.dto.response.ProductionLineDto;
import fpt.qn.mes.master.line.application.exception.LineConflictException;
import fpt.qn.mes.master.line.application.exception.LineNotFoundException;
import fpt.qn.mes.master.line.application.mapper.LineDtoMapper;
import fpt.qn.mes.master.line.domain.entities.ProductionLine;
import fpt.qn.mes.master.line.domain.repository.ProductionLineRepository;

@ExtendWith(MockitoExtension.class)
class LineServiceTest {

    @Mock ProductionLineRepository lineRepository;
    @Mock LineDtoMapper mapper;
    @Mock DSLContext ctx;
    @InjectMocks LineService lineService;

    UUID id = UUID.randomUUID();
    UUID userId = UUID.randomUUID();
    UUID statusId = UUID.randomUUID();
    ProductionLine line;
    ProductionLineDto dto;

    @BeforeEach
    void setUp() {
        line = ProductionLine.create("LN-TEST", "Test Line", statusId, userId);
        dto = ProductionLineDto.builder().id(id).code("LN-TEST").build();
    }

    @Test
    void createLine_Success() {
        when(lineRepository.existsByCode("LN-TEST")).thenReturn(false);
        when(lineRepository.save(any())).thenReturn(line);
        when(mapper.toDto(any())).thenReturn(dto);

        var req = new CreateLineRequest();
        req.setCode("LN-TEST"); req.setName("Test"); req.setLineStatusId(statusId);

        var result = lineService.createLine(req, userId);
        assertNotNull(result);
    }

    @Test
    void createLine_DuplicateCode_ThrowsConflict() {
        when(lineRepository.existsByCode("LN-TEST")).thenReturn(true);
        var req = new CreateLineRequest();
        req.setCode("LN-TEST"); req.setName("Test"); req.setLineStatusId(statusId);
        assertThrows(LineConflictException.class, () -> lineService.createLine(req, userId));
    }

    @Test
    void getLineById_NotFound_Throws404() {
        when(lineRepository.findById(id)).thenReturn(Optional.empty());
        assertThrows(LineNotFoundException.class, () -> lineService.getLineById(id));
    }
}
