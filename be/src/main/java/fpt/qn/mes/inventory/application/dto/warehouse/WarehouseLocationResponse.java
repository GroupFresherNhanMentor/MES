package fpt.qn.mes.inventory.application.dto.warehouse;

import java.util.UUID;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class WarehouseLocationResponse {
    UUID id;
    String code;
    String name;
}
