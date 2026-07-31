package fpt.qn.mes.master.warehouse.application.dto.warehousestatus.search;

import fpt.qn.mes.common.dto.request.BaseSearchRequest;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

@Getter @Setter @FieldDefaults(level = AccessLevel.PRIVATE)
public class WarehouseStatusSearchRequest extends BaseSearchRequest {
    String name;
}
