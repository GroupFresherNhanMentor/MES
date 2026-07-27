package fpt.qn.mes.inventory.application.dto.request;

import java.time.LocalDate;
import java.util.UUID;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter @Setter @FieldDefaults(level = AccessLevel.PRIVATE)
public class CreateStockLotRequest {
    @NotBlank String lotNumber;
    @NotNull UUID productId;
    UUID lotTypeId;
    LocalDate expiryDate;
}
