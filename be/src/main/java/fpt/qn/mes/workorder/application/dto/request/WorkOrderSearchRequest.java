package fpt.qn.mes.workorder.application.dto.request;

import java.util.UUID;
import fpt.qn.mes.common.dto.request.BaseSearchRequest;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class WorkOrderSearchRequest extends BaseSearchRequest {
    UUID finishedProductId;
    UUID statusId;
    String code;
}
