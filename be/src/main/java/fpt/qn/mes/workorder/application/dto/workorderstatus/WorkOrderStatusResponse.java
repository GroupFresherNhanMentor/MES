package fpt.qn.mes.workorder.application.dto.workorderstatus;

import java.util.UUID;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class WorkOrderStatusResponse {
    UUID id;
    String name;
    String description;
    boolean isInitial;
    boolean isFinal;
}
