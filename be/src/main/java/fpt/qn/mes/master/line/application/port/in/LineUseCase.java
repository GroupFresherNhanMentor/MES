package fpt.qn.mes.master.line.application.port.in;

import java.util.UUID;
import fpt.qn.mes.common.dto.response.PageResponse;
import fpt.qn.mes.master.line.application.dto.line.LineResponse;
import fpt.qn.mes.master.line.application.dto.line.create.CreateLineRequest;
import fpt.qn.mes.master.line.application.dto.line.search.LineSearchRequest;
import fpt.qn.mes.master.line.application.dto.line.update.UpdateLineRequest;

public interface LineUseCase {
    PageResponse<LineResponse> getLines(LineSearchRequest request);
    LineResponse getLineById(UUID id);
    void createLine(CreateLineRequest request);
    void updateLine(UUID id, UpdateLineRequest request);
    void activateLine(UUID id);
    void deactivateLine(UUID id);
}
