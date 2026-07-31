package fpt.qn.mes.master.line.application.dto.response;

import java.time.Instant;
import java.util.UUID;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor @FieldDefaults(level = AccessLevel.PRIVATE)
public class ProductionLineDto {
    UUID id; String code; String name; UUID lineStatusId; String lineStatusName;
    Instant createdAt; UUID createdBy; Instant updatedAt; UUID updatedBy;
}
