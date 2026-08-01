package fpt.qn.mes.master.warehouse.application.dto.warehouse.assign;

import java.util.UUID;

import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AssignManagerRequest {

    @NotNull
    UUID userId;
}
