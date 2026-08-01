package fpt.qn.mes.master.location.domain.repository.criteria;

import java.util.UUID;
import fpt.qn.mes.common.domainQuery.BaseSearchCriteria;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;

@Getter @Setter @SuperBuilder @NoArgsConstructor @AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class WarehouseLocationSearchCriteria extends BaseSearchCriteria {
    UUID warehouseId;
    String code;
    String name;
    UUID locationStatusId;
}
