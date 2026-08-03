package fpt.qn.mes.workorder.application.dto.response;

import java.util.List;
import java.util.UUID;

import fpt.qn.mes.workorder.application.dto.workorderstatus.WorkOrderStatusResponse;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.experimental.FieldDefaults;

@Getter
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ReserveWorkOrderMaterialsResponse {
    UUID workOrderId;
    WorkOrderStatusResponse status;
    List<ReservedMaterialAllocationResponse> allocations;
}
