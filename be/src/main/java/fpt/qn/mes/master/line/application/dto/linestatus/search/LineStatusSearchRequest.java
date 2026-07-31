package fpt.qn.mes.master.line.application.dto.linestatus.search;

import fpt.qn.mes.common.dto.request.BaseSearchRequest;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

@Getter @Setter @FieldDefaults(level = AccessLevel.PRIVATE)
public class LineStatusSearchRequest extends BaseSearchRequest {
    String name;
}
