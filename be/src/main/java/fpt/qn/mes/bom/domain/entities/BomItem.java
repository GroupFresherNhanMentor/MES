package fpt.qn.mes.bom.domain.entities;

import java.math.BigDecimal;
import java.util.UUID;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter @Builder @FieldDefaults(level = AccessLevel.PRIVATE)
public class BomItem {
    UUID id; UUID bomId; UUID materialProductId; BigDecimal quantityPerUnit; String unit; BigDecimal scrapRate;

    public static BomItem create(UUID bomId, UUID materialProductId, BigDecimal quantityPerUnit, String unit, BigDecimal scrapRate) {
        throw new UnsupportedOperationException("Not implemented");
    }
}
