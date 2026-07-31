package fpt.qn.mes.master.warehouse.application.dto.warehousestatus.create;

import jakarta.validation.constraints.NotBlank;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

@Getter @Setter @FieldDefaults(level = AccessLevel.PRIVATE)
public class CreateWarehouseStatusRequest {
    @NotBlank String name;
    String description;
}
