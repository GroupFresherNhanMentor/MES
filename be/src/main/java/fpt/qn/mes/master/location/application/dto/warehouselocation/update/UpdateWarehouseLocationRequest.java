package fpt.qn.mes.master.location.application.dto.warehouselocation.update;

import java.util.UUID;
import jakarta.validation.constraints.NotBlank;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

@Getter @Setter @FieldDefaults(level = AccessLevel.PRIVATE)
public class UpdateWarehouseLocationRequest {
    @NotBlank String name;
    UUID locationStatusId;
}
