package fpt.qn.mes.quality.application.port.in;

import fpt.qn.mes.common.dto.response.PageResponse;
import fpt.qn.mes.quality.application.dto.qcstatus.QcStatusResponse;
import fpt.qn.mes.quality.application.dto.qcstatus.create.CreateQcStatusRequest;
import fpt.qn.mes.quality.application.dto.qcstatus.search.QcStatusSearchRequest;

public interface QcStatusUseCase {
    PageResponse<QcStatusResponse> getQcStatuses(QcStatusSearchRequest request);
    void createQcStatus(CreateQcStatusRequest request);
}
