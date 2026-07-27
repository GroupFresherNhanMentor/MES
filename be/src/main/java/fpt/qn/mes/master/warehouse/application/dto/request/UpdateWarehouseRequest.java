package fpt.qn.mes.master.warehouse.application.dto.request;

import java.util.UUID;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter @Setter @FieldDefaults(level = AccessLevel.PRIVATE)
public class UpdateWarehouseRequest {
    String name; String address; UUID warehouseStatusId;
}
