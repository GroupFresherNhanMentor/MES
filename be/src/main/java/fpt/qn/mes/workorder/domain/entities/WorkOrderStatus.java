package fpt.qn.mes.workorder.domain.entities;

import java.util.UUID;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.experimental.FieldDefaults;

@Getter
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class WorkOrderStatus {
    UUID id;
    String name;
    String description;
}
