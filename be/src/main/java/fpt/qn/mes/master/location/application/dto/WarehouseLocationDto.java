package fpt.qn.mes.master.location.application.dto;

import java.time.Instant;
import java.util.UUID;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter @Builder @NoArgsConstructor @AllArgsConstructor @FieldDefaults(level = AccessLevel.PRIVATE)
public class WarehouseLocationDto {
    UUID id; UUID warehouseId; String code; String name;
    UUID locationStatusId; Instant createdAt; UUID createdBy; Instant updatedAt; UUID updatedBy;
}
