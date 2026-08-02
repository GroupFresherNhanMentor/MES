package fpt.qn.mes.master.location.application.dto.warehouselocation.create;

import jakarta.validation.constraints.NotBlank;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

@Getter @Setter @FieldDefaults(level = AccessLevel.PRIVATE)
public class CreateWarehouseLocationRequest {
    @NotBlank String code;
    @NotBlank String name;
}
