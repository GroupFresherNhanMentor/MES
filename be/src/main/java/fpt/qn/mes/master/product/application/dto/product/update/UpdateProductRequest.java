package fpt.qn.mes.master.product.application.dto.product.update;

import java.util.UUID;

import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UpdateProductRequest {
    @Size(min = 1, max = 255, message = "Name must be between 1 and 255 characters")
    String name;
    UUID productTypeId;
    UUID unitId;
}
