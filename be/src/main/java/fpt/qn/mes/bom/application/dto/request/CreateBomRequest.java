package fpt.qn.mes.bom.application.dto.request;

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

    @NotNull(message = "finishedProductId is required")
    UUID finishedProductId;

    Integer version;

    UUID bomStatusId;
}
