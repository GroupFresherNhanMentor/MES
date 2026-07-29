package fpt.qn.mes.inventory.domain.repository.criteria;

import java.time.LocalDate;
import java.util.UUID;

import fpt.qn.mes.common.domainQuery.BaseSearchCriteria;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class StockLotSearchCriteria extends BaseSearchCriteria {

    UUID productId;
    UUID lotTypeId;
    String lotNumber;
    LocalDate expiryBefore;
}
