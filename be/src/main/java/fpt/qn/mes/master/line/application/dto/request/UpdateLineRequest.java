package fpt.qn.mes.master.line.application.dto.request;

import java.util.UUID;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter @Setter @FieldDefaults(level = AccessLevel.PRIVATE)
public class UpdateLineRequest {
    String name; UUID lineStatusId;
}
