package fpt.qn.mes.workorder.application.dto.request;

import java.util.UUID;

import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ReserveWorkOrderMaterialsRequest {

    @NotNull(message = "machineId is required")
    UUID machineId;
}
