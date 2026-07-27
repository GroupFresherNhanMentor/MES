package fpt.qn.mes.master.product.application.dto.request;

import java.util.UUID;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter @Setter @FieldDefaults(level = AccessLevel.PRIVATE)
public class UpdateProductRequest {
    String name;
    String unit;
    UUID productStatusId;
}
