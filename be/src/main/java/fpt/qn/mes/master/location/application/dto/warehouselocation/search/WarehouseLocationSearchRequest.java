package fpt.qn.mes.master.location.application.dto.warehouselocation.search;

import java.util.UUID;
import fpt.qn.mes.common.dto.request.BaseSearchRequest;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

@Getter @Setter @FieldDefaults(level = AccessLevel.PRIVATE)
public class WarehouseLocationSearchRequest extends BaseSearchRequest {
    String code;
    String name;
    UUID locationStatusId;
}
