package fpt.qn.mes.workorder.domain.repository;

import java.util.Optional;
import java.util.UUID;

import fpt.qn.mes.common.domainQuery.PaginationResult;
import fpt.qn.mes.workorder.domain.entities.WorkOrder;
import fpt.qn.mes.workorder.domain.entities.WorkOrderEvent;
import fpt.qn.mes.workorder.domain.entities.WorkOrderMaterial;

import fpt.qn.mes.workorder.domain.repository.criteria.WorkOrderSearchCriteria;

public interface WorkOrderRepository {
    Optional<WorkOrder> findById(UUID id);
    Optional<WorkOrder> findForUpdate(UUID id);
    WorkOrder save(WorkOrder workOrder);
    WorkOrder update(WorkOrder workOrder);
    void deleteById(UUID id);
    PaginationResult<WorkOrder> findAll(WorkOrderSearchCriteria criteria);

    WorkOrderMaterial saveMaterial(WorkOrderMaterial material);
    WorkOrderMaterial updateMaterial(WorkOrderMaterial material);
    Optional<WorkOrderMaterial> findMaterialById(UUID materialId);
    PaginationResult<WorkOrderMaterial> findMaterialsByWorkOrderId(UUID workOrderId, int page, int size);
    java.util.List<WorkOrderMaterial> findMaterialsByWorkOrderId(UUID workOrderId);
    void deleteMaterialById(UUID materialId);

    WorkOrderEvent saveEvent(WorkOrderEvent event);
    PaginationResult<WorkOrderEvent> findEventsByWorkOrderId(UUID workOrderId, int page, int size);
    java.util.List<WorkOrderEvent> findEventsByWorkOrderId(UUID workOrderId);
    Optional<String> findStatusNameById(UUID id);
    Optional<UUID> findStatusIdByName(String name);
    boolean hasActiveTransition(UUID fromStatusId, UUID toStatusId);
    boolean existsByCodeAndIdNot(String code, UUID excludeId);
}
