package fpt.qn.mes.master.line.application.service;

import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import fpt.qn.mes.common.dto.PageResponse;
import fpt.qn.mes.master.line.application.dto.CreateLineRequest;
import fpt.qn.mes.master.line.application.dto.ProductionLineDto;
import fpt.qn.mes.master.line.application.dto.UpdateLineRequest;
import fpt.qn.mes.master.line.application.mapper.LineDtoMapper;
import fpt.qn.mes.master.line.application.port.in.LineUseCase;
import fpt.qn.mes.master.line.domain.repository.ProductionLineRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class LineService implements LineUseCase {

    ProductionLineRepository lineRepository;
    LineDtoMapper mapper;

    @Override @Transactional(readOnly = true)
    public PageResponse<ProductionLineDto> getLines(int page, int size) {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override @Transactional(readOnly = true)
    public ProductionLineDto getLineById(UUID id) {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override @Transactional
    public ProductionLineDto createLine(CreateLineRequest request, UUID currentUserId) {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override @Transactional
    public ProductionLineDto updateLine(UUID id, UpdateLineRequest request, UUID currentUserId) {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override @Transactional
    public void deleteLine(UUID id) {}
}
