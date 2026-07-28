package fpt.qn.mes.bom.domain.entities;

import java.math.BigDecimal;
import java.util.UUID;

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
    BigDecimal quantityPerUnit;
    String unit;
    BigDecimal scrapRate;

    public static BomItem create(UUID bomId, UUID materialProductId, BigDecimal quantityPerUnit, String unit, BigDecimal scrapRate) {
        return BomItem.builder()
                .id(UUID.randomUUID())
                .bomId(bomId)
                .materialProductId(materialProductId)
                .quantityPerUnit(quantityPerUnit)
                .unit(unit)
                .scrapRate(scrapRate != null ? scrapRate : BigDecimal.ZERO)
                .build();
    }
}
