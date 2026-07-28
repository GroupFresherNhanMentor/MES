package fpt.qn.mes.master.product.application.dto.request;

import java.util.UUID;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter @Setter @FieldDefaults(level = AccessLevel.PRIVATE)
public class CreateProductRequest {
    @NotBlank String code;
    @NotBlank String name;
    @NotNull UUID productTypeId;
    @NotNull UUID unitId;
    @NotNull UUID productStatusId;
}
