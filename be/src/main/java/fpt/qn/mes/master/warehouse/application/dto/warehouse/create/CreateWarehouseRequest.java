package fpt.qn.mes.master.warehouse.application.dto.warehouse.create;

import java.util.UUID;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

@Getter @Setter @FieldDefaults(level = AccessLevel.PRIVATE)
public class CreateWarehouseRequest {
    @NotBlank String code;
    @NotBlank String name;
    @NotBlank String address;
    @NotNull UUID warehouseStatusId;
}
