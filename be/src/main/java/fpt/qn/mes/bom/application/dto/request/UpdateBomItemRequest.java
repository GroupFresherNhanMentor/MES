package fpt.qn.mes.bom.application.dto.request;

import java.math.BigDecimal;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter @Setter @FieldDefaults(level = AccessLevel.PRIVATE)
public class UpdateBomItemRequest {
    @NotNull @Positive BigDecimal quantityPerUnit;
    String unit;
    BigDecimal scrapRate;
}
