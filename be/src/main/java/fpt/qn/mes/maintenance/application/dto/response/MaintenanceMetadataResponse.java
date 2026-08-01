package fpt.qn.mes.maintenance.application.dto.response;

import java.util.UUID;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.experimental.FieldDefaults;

@Getter
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class MaintenanceMetadataResponse {
    UUID id;
    String name;
    String description;
}