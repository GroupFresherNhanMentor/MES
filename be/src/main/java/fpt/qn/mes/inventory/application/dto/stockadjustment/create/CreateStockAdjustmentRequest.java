package fpt.qn.mes.inventory.application.dto.stockadjustment.create;

import java.math.BigDecimal;
import java.util.UUID;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CreateStockAdjustmentRequest {

    @NotNull(message = "Stock balance ID is required")
    UUID stockBalanceId;

    @NotNull(message = "Quantity adjustment is required")
    BigDecimal quantityAdjustment;

    @NotBlank(message = "Reason is required for stock adjustment")
    String reason;

    String referenceNo;
}
