package fpt.qn.mes.quality.application.port.in;

import fpt.qn.mes.common.dto.response.PageResponse;
import fpt.qn.mes.quality.application.dto.qcaction.QcActionResponse;
import fpt.qn.mes.quality.application.dto.qcaction.create.CreateQcActionRequest;
import fpt.qn.mes.quality.application.dto.qcaction.search.QcActionSearchRequest;

public interface QcActionUseCase {
    PageResponse<QcActionResponse> getQcActions(QcActionSearchRequest request);
    void createQcAction(CreateQcActionRequest request);
}
