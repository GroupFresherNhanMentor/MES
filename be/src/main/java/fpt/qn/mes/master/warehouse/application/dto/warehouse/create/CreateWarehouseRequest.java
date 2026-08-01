package fpt.qn.mes.master.warehouse.application.dto.warehouse.create;

import jakarta.validation.constraints.NotBlank;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

@Getter @Setter @FieldDefaults(level = AccessLevel.PRIVATE)
public class CreateWarehouseRequest {
    @NotBlank String code;
    @NotBlank String name;
    @NotBlank String address;
}
