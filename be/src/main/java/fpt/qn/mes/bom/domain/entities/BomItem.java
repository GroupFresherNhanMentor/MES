package fpt.qn.mes.bom.domain.entities;

import java.math.BigDecimal;
import java.util.UUID;

import fpt.qn.mes.common.util.UuidV7;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.experimental.FieldDefaults;

@Getter
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class BomItem {
    UUID id;
    UUID bomId;
    UUID materialProductId;
    String materialProductCode;
    String materialProductName;
    BigDecimal quantityPerUnit;
    String unitName;
    BigDecimal scrapRate;

    public static BomItem create(UUID bomId, UUID materialProductId, BigDecimal quantityPerUnit, BigDecimal scrapRate) {
        return BomItem.builder()
                .id(UuidV7.generate())
                .bomId(bomId)
                .materialProductId(materialProductId)
                .quantityPerUnit(quantityPerUnit)
                .scrapRate(scrapRate != null ? scrapRate : BigDecimal.ZERO)
                .build();
    }
}
