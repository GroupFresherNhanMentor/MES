package fpt.qn.mes.workorder.application.dto.response;

import java.util.UUID;
import java.util.List;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.experimental.FieldDefaults;

@Getter
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ReserveWorkOrderMaterialsResponse {
    UUID workOrderId;
    String status;
    List<ReservedMaterialAllocationResponse> allocations;
}
