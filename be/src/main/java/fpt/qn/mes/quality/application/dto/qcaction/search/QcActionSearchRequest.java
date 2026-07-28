package fpt.qn.mes.quality.application.dto.qcaction.search;

import fpt.qn.mes.common.dto.request.BaseSearchRequest;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class QcActionSearchRequest extends BaseSearchRequest {
    String name;
}
