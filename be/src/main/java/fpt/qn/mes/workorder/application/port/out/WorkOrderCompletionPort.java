package fpt.qn.mes.workorder.application.port.out;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import fpt.qn.mes.workorder.application.dto.workorder.complete.CompleteWorkOrderRequest;
import fpt.qn.mes.workorder.application.port.out.dto.ActiveProductionRun;
import fpt.qn.mes.workorder.application.port.out.dto.CompletionOutputDestination;
import fpt.qn.mes.workorder.application.port.out.dto.CompletionReferences;
import fpt.qn.mes.workorder.application.port.out.dto.CompletionReservationAllocation;
import fpt.qn.mes.workorder.domain.entities.WorkOrder;

public interface WorkOrderCompletionPort {
    Optional<CompletionOutputDestination> findOutputDestination(UUID warehouseId, UUID locationId);

    List<CompletionReservationAllocation> findOutstandingReservations(UUID workOrderId);

    CompletionReferences findCompletionReferences();

    boolean finalizeCompletion(WorkOrder workOrder, ActiveProductionRun productionRun,
            CompleteWorkOrderRequest request, List<CompletionReservationAllocation> allocations,
            CompletionOutputDestination destination, CompletionReferences references, UUID actorId);
}
