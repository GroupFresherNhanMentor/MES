package fpt.qn.mes.master.product.application.dto.product.search;

import java.util.UUID;

import fpt.qn.mes.common.dto.request.BaseSearchRequest;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ProductSearchRequest extends BaseSearchRequest {
    String code;
    String name;
    String version;
    UUID productTypeId;
    UUID unitId;
    UUID productStatusId;
}
