package fpt.qn.mes.master.product.application.dto.product.create;

import java.util.UUID;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CreateProductRequest {
    @NotBlank
    String code;
    @NotBlank
    String name;
    @NotBlank
    String version;
    @NotNull
    UUID productTypeId;
    @NotNull
    UUID unitId;
}
