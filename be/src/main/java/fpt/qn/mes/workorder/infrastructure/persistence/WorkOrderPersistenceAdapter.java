package fpt.qn.mes.workorder.infrastructure.persistence;

import static fpt.qn.mes.jooq.Tables.WORK_ORDERS;

import java.util.Optional;
import java.util.UUID;

import org.jooq.DSLContext;
import org.springframework.stereotype.Repository;

import fpt.qn.mes.common.dto.PaginationResult;
import fpt.qn.mes.common.repository.BaseRepository;
import fpt.qn.mes.jooq.tables.records.WorkOrdersRecord;
import fpt.qn.mes.workorder.domain.entities.WorkOrder;
import fpt.qn.mes.workorder.domain.entities.WorkOrderEvent;
import fpt.qn.mes.workorder.domain.entities.WorkOrderMaterial;
import fpt.qn.mes.workorder.domain.repository.WorkOrderRepository;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;

@Repository
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class WorkOrderPersistenceAdapter extends BaseRepository<WorkOrdersRecord> implements WorkOrderRepository {

    WorkOrderRecordMapper mapper;
    DSLContext dslCtx;

    public WorkOrderPersistenceAdapter(DSLContext ctx, WorkOrderRecordMapper mapper) {
        super(ctx, WORK_ORDERS); this.mapper = mapper; this.dslCtx = ctx;
    }

    @Override public Optional<WorkOrder> findById(UUID id) { throw new UnsupportedOperationException("Not implemented"); }
    @Override public WorkOrder save(WorkOrder w) { throw new UnsupportedOperationException("Not implemented"); }
    @Override public WorkOrder update(WorkOrder w) { throw new UnsupportedOperationException("Not implemented"); }
    @Override public void deleteById(UUID id) {}
    @Override public PaginationResult<WorkOrder> findAll(int page, int size) { throw new UnsupportedOperationException("Not implemented"); }
    @Override public WorkOrderMaterial saveMaterial(WorkOrderMaterial m) { throw new UnsupportedOperationException("Not implemented"); }
    @Override public Optional<WorkOrderMaterial> findMaterialById(UUID materialId) { throw new UnsupportedOperationException("Not implemented"); }
    @Override public PaginationResult<WorkOrderMaterial> findMaterialsByWorkOrderId(UUID workOrderId, int page, int size) { throw new UnsupportedOperationException("Not implemented"); }
    @Override public void deleteMaterialById(UUID materialId) {}
    @Override public WorkOrderEvent saveEvent(WorkOrderEvent e) { throw new UnsupportedOperationException("Not implemented"); }
    @Override public PaginationResult<WorkOrderEvent> findEventsByWorkOrderId(UUID workOrderId, int page, int size) { throw new UnsupportedOperationException("Not implemented"); }
}
