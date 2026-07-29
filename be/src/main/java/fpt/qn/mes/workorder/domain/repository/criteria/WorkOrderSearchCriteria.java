package fpt.qn.mes.workorder.domain.repository.criteria;

import java.util.UUID;
import fpt.qn.mes.common.domainQuery.BaseSearchCriteria;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder
public class WorkOrderSearchCriteria extends BaseSearchCriteria {
    UUID finishedProductId;
    UUID statusId;
    String code;
}
