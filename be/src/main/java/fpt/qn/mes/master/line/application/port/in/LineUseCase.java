package fpt.qn.mes.master.line.application.port.in;

import java.util.UUID;

import fpt.qn.mes.common.dto.PageResponse;
import fpt.qn.mes.master.line.application.dto.CreateLineRequest;
import fpt.qn.mes.master.line.application.dto.ProductionLineDto;
import fpt.qn.mes.master.line.application.dto.UpdateLineRequest;

public interface LineUseCase {
    PageResponse<ProductionLineDto> getLines(int page, int size);
    ProductionLineDto getLineById(UUID id);
    ProductionLineDto createLine(CreateLineRequest request, UUID currentUserId);
    ProductionLineDto updateLine(UUID id, UpdateLineRequest request, UUID currentUserId);
    void deleteLine(UUID id);
}
