package fpt.qn.mes.bom.application.dto.response;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter @Builder @NoArgsConstructor @AllArgsConstructor @FieldDefaults(level = AccessLevel.PRIVATE)
public class BomResponse {
    UUID id;
    UUID finishedProductId;
    String finishedProductCode;
    String finishedProductName;
    Integer version;
    UUID bomStatusId;
    String bomStatusName;
    UUID createdBy;
    Instant createdAt;
    List<BomItemResponse> items;
}
