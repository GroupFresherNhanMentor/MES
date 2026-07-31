package fpt.qn.mes.master.product.application.dto.unitofmeasure.create;

import jakarta.validation.constraints.NotBlank;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CreateUnitOfMeasureRequest {
    @NotBlank String name;
    String description;
}
