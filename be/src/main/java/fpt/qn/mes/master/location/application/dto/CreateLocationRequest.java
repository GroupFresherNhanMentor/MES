package fpt.qn.mes.master.location.application.dto;

import java.util.UUID;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter @Setter @FieldDefaults(level = AccessLevel.PRIVATE)
public class CreateLocationRequest {
    @NotBlank String code;
    String name;
    @NotNull UUID locationStatusId;
}
