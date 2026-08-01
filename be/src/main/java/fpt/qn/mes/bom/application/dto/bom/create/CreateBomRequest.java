package fpt.qn.mes.bom.application.dto.bom.create;

import java.util.UUID;

import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CreateBomRequest {
    @NotNull
    UUID finishedProductId;
    Integer version;
}
