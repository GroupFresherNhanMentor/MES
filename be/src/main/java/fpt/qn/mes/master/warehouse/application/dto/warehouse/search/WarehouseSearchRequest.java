package fpt.qn.mes.master.warehouse.application.dto.warehouse.search;

import java.util.UUID;
import fpt.qn.mes.common.dto.request.BaseSearchRequest;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

@Getter @Setter @FieldDefaults(level = AccessLevel.PRIVATE)
public class WarehouseSearchRequest extends BaseSearchRequest {
    String code;
    String name;
    UUID warehouseStatusId;
}
