package fpt.qn.mes.master.warehouse.application.dto.response;

import java.time.Instant;
import java.util.UUID;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter @Builder @NoArgsConstructor @AllArgsConstructor @FieldDefaults(level = AccessLevel.PRIVATE)
public class WarehouseDto {
    UUID id; String code; String name; String address;
    UUID warehouseStatusId; Instant createdAt; UUID createdBy; Instant updatedAt; UUID updatedBy;
}
