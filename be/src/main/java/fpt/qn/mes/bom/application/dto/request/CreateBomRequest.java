package fpt.qn.mes.bom.application.dto.request;

import java.util.UUID;

import jakarta.validation.constraints.Min;
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

    @NotNull(message = "version is required")
    @Min(value = 1, message = "version must be at least 1")
    Integer version;

    UUID bomStatusId;
}
