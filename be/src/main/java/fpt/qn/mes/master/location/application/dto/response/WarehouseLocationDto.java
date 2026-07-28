package fpt.qn.mes.master.location.application.dto.response;

import java.time.Instant;
import java.util.UUID;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter @Builder @NoArgsConstructor @AllArgsConstructor @FieldDefaults(level = AccessLevel.PRIVATE)
public class WarehouseLocationDto {
    UUID id; UUID warehouseId; String code; String name;
    UUID locationStatusId; String locationStatusName;
    Instant createdAt; UUID createdBy; Instant updatedAt; UUID updatedBy;
}
