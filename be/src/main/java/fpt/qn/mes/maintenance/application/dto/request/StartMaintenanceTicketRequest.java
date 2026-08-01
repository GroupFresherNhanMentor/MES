package fpt.qn.mes.maintenance.application.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import java.util.UUID;

@Getter
@Setter
public class StartMaintenanceTicketRequest {
    @NotNull(message = "Assigned engineer ID không được để trống")
    UUID assignedEngineerId;
}