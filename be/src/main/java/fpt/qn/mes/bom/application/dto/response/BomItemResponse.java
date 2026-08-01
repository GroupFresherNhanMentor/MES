package fpt.qn.mes.bom.application.dto.response;

import java.math.BigDecimal;
import java.util.UUID;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter @Builder @NoArgsConstructor @AllArgsConstructor @FieldDefaults(level = AccessLevel.PRIVATE)
public class BomItemResponse {
    UUID id;
    UUID bomId;
    UUID materialProductId;
    String materialProductCode;
    String materialProductName;
    BigDecimal quantityPerUnit;
    UUID unitId;
    String unit;
    BigDecimal scrapRate;
}
