package fpt.qn.mes.maintenance.application.dto.request;

import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CloseMaintenanceTicketRequest {
    UUID ticketId;
    String actionTaken;
    String rootCause;
    String note;
    String machineResolutionStatus; // "AVAILABLE" hoặc "DOWN" dựa trên kết quả thực tế
}