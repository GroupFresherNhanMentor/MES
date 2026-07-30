package fpt.qn.mes.inventory.application.dto.response;

import java.util.UUID;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ProductSummaryDto {
    UUID id;
    String code;
    String name;
}
