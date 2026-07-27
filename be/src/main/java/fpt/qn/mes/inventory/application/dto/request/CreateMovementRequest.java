package fpt.qn.mes.inventory.application.dto.request;

import java.math.BigDecimal;
import java.util.UUID;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter @Setter @FieldDefaults(level = AccessLevel.PRIVATE)
public class CreateMovementRequest {
    @NotNull UUID movementTypeId;
    @NotNull UUID productId;
    UUID lotId;
    @NotNull UUID warehouseId;
    UUID locationId;
    @NotNull @Positive BigDecimal quantity;
    UUID fromStatusId;
    UUID toStatusId;
    String referenceType;
    UUID referenceId;
    String reason;
}
