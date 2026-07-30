package fpt.qn.mes.inventory.application.dto.request;

import java.math.BigDecimal;
import java.util.UUID;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
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
public class StockTransferRequest {

    @NotNull(message = "From warehouse ID is required")
    UUID fromWarehouseId;

    @NotNull(message = "From location ID is required")
    UUID fromLocationId;

    @NotNull(message = "To warehouse ID is required")
    UUID toWarehouseId;

    @NotNull(message = "To location ID is required")
    UUID toLocationId;

    @NotNull(message = "Product ID is required")
    UUID productId;

    @NotNull(message = "Lot ID is required")
    UUID lotId;

    @NotNull(message = "Quantity is required")
    @Positive(message = "Quantity must be greater than zero")
    BigDecimal quantity;
}
