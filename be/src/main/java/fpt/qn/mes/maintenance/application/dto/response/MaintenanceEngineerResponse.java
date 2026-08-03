package fpt.qn.mes.maintenance.application.dto.response;

import java.util.UUID;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class MaintenanceEngineerResponse {
    UUID id;
    String username;
    String fullName;
}
