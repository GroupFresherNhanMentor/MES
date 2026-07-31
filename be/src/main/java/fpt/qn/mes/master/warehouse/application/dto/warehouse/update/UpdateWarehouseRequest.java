package fpt.qn.mes.master.warehouse.application.dto.warehouse.update;

import java.util.UUID;
import jakarta.validation.constraints.NotBlank;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

@Getter @Setter @FieldDefaults(level = AccessLevel.PRIVATE)
public class UpdateWarehouseRequest {
    @NotBlank String name;
    @NotBlank String address;
    UUID warehouseStatusId;
}
