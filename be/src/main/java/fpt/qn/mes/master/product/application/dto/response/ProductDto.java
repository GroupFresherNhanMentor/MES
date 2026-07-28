package fpt.qn.mes.master.product.application.dto.response;

import java.time.Instant;
import java.util.UUID;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter @Builder @NoArgsConstructor @AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ProductDto {
    UUID id;
    String code;
    String name;
    UUID productTypeId;
    String productTypeName;
    UUID unitId;
    String unitName;
    UUID productStatusId;
    String productStatusName;
    Long version;
    Instant createdAt;
    UUID createdBy;
    Instant updatedAt;
    UUID updatedBy;
}
