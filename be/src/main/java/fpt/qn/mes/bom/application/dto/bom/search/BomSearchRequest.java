package fpt.qn.mes.bom.application.dto.bom.search;

import java.util.UUID;

import fpt.qn.mes.common.dto.request.BaseSearchRequest;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class BomSearchRequest extends BaseSearchRequest {
    UUID finishedProductId;
    UUID bomStatusId;
}
