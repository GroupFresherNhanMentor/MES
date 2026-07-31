package fpt.qn.mes.master.warehouse.domain.repository.criteria;

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
public class WarehouseSearchCriteria extends BaseSearchCriteria {
    String code;
    String name;
    UUID warehouseStatusId;
}
