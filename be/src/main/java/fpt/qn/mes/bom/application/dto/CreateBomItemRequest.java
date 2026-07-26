package fpt.qn.mes.bom.application.dto;

import java.math.BigDecimal;
import java.util.UUID;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter @Setter @FieldDefaults(level = AccessLevel.PRIVATE)
public class CreateBomItemRequest {
    @NotNull UUID materialProductId;
    @NotNull @Positive BigDecimal quantityPerUnit;
    String unit;
    BigDecimal scrapRate;
}
