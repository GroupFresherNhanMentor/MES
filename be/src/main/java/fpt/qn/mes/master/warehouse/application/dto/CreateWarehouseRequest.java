package fpt.qn.mes.master.warehouse.application.dto;

import java.util.UUID;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter @Setter @FieldDefaults(level = AccessLevel.PRIVATE)
public class CreateWarehouseRequest {
    @NotBlank String code;
    @NotBlank String name;
    String address;
    @NotNull UUID warehouseStatusId;
}
