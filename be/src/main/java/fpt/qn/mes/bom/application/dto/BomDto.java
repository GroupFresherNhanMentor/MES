package fpt.qn.mes.bom.application.dto;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter @Builder @NoArgsConstructor @AllArgsConstructor @FieldDefaults(level = AccessLevel.PRIVATE)
public class BomDto {
    UUID id; UUID finishedProductId; Integer version; UUID bomStatusId;
    UUID createdBy; Instant createdAt; List<BomItemDto> items;
}
