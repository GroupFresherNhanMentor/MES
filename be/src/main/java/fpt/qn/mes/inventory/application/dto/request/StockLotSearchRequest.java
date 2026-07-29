package fpt.qn.mes.inventory.application.dto.request;

import java.time.LocalDate;
import java.util.UUID;

import fpt.qn.mes.common.dto.request.BaseSearchRequest;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class StockLotSearchRequest extends BaseSearchRequest {

    UUID productId;
    UUID lotTypeId;
    String lotNumber;
    LocalDate expiryBefore;
}
