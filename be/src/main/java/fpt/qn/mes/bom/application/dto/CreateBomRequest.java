package fpt.qn.mes.bom.application.dto;

import java.util.UUID;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter @Setter @FieldDefaults(level = AccessLevel.PRIVATE)
public class CreateBomRequest {
    @NotNull UUID finishedProductId;
    @NotNull Integer version;
    @NotNull UUID bomStatusId;
}
