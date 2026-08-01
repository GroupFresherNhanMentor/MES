package fpt.qn.mes.master.product.application.dto.unitofmeasure.search;

import fpt.qn.mes.common.dto.request.BaseSearchRequest;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UnitOfMeasureSearchRequest extends BaseSearchRequest {
    String name;
}
