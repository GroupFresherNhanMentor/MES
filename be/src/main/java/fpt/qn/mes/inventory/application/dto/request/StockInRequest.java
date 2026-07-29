package fpt.qn.mes.inventory.application.dto.request;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import jakarta.validation.constraints.DecimalMin;
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
public class StockInRequest {

    @NotNull(message = "Product ID is required")
    UUID productId;

    @NotNull(message = "Warehouse ID is required")
    UUID warehouseId;

    @NotNull(message = "Location ID is required")
    UUID locationId;

    @NotBlank(message = "Lot number is required")
    String lotNumber;

    UUID lotTypeId;

    LocalDate expiryDate;

    @NotNull(message = "Quantity is required")
    @DecimalMin(value = "0.0001", message = "Quantity must be greater than zero")
    BigDecimal quantity;

    @NotBlank(message = "Reference number is required")
    String referenceNo;

    String reason;
}
