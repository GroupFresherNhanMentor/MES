package fpt.qn.mes.bom.application.dto.response;

import java.math.BigDecimal;
import java.util.UUID;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter @Builder @NoArgsConstructor @AllArgsConstructor @FieldDefaults(level = AccessLevel.PRIVATE)
public class BomItemDto {
    UUID id; UUID bomId; UUID materialProductId; BigDecimal quantityPerUnit; String unit; BigDecimal scrapRate;
}
