package fpt.qn.mes.bom.application.dto.bomitem;

import java.math.BigDecimal;
import java.util.UUID;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class BomItemResponse {
    UUID id;
    UUID bomId;
    UUID materialProductId;
    String materialProductCode;
    String materialProductName;
    BigDecimal quantityPerUnit;
    String unitName;
    BigDecimal scrapRate;
}
