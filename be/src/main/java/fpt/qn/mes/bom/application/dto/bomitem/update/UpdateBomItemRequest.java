package fpt.qn.mes.bom.application.dto.bomitem.update;

import java.math.BigDecimal;
import java.util.UUID;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UpdateBomItemRequest {
    @NotNull
    UUID materialProductId;
    @NotNull
    @Positive
    BigDecimal quantityPerUnit;
    BigDecimal scrapRate;
}
