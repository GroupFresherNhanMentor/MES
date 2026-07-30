package fpt.qn.mes.workorder.application.dto.request;

import java.util.UUID;

import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class StartWorkOrderRequest {

    @NotNull(message = "machineId is required")
    UUID machineId;

    @NotNull(message = "productionLineId is required")
    UUID productionLineId;

    UUID operatorId;
}
