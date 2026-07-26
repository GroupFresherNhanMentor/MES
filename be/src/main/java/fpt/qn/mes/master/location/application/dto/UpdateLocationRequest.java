package fpt.qn.mes.master.location.application.dto;

import java.util.UUID;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter @Setter @FieldDefaults(level = AccessLevel.PRIVATE)
public class UpdateLocationRequest {
    String name; UUID locationStatusId;
}
