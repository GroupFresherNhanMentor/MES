package fpt.qn.mes.master.line.application.port.in;

import fpt.qn.mes.common.dto.response.PageResponse;
import fpt.qn.mes.master.line.application.dto.linestatus.LineStatusResponse;
import fpt.qn.mes.master.line.application.dto.linestatus.create.CreateLineStatusRequest;
import fpt.qn.mes.master.line.application.dto.linestatus.search.LineStatusSearchRequest;

public interface LineStatusUseCase {
    PageResponse<LineStatusResponse> getLineStatuses(LineStatusSearchRequest request);
    void createLineStatus(CreateLineStatusRequest request);
}
