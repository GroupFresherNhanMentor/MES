package fpt.qn.mes.inventory.infrastructure.persistence;

import java.time.ZoneOffset;

import org.springframework.stereotype.Component;

import fpt.qn.mes.inventory.domain.entities.StockAdjustmentApproval;
import fpt.qn.mes.jooq.tables.records.StockAdjustmentApprovalsRecord;

@Component
public class StockAdjustmentApprovalRecordMapper {

    public StockAdjustmentApproval toDomain(StockAdjustmentApprovalsRecord r) {
        if (r == null) return null;
        return StockAdjustmentApproval.builder()
                .id(r.getId())
                .productId(r.getProductId())
                .warehouseId(r.getWarehouseId())
                .locationId(r.getLocationId())
                .stockBalanceId(r.getStockBalanceId())
                .quantityAdjustment(r.getQuantityAdjustment())
                .reason(r.getReason())
                .referenceNo(r.getReferenceNo())
                .createdBy(r.getCreatedBy())
                .createdAt(r.getCreatedAt() != null ? r.getCreatedAt().toInstant() : null)
                .build();
    }

    public StockAdjustmentApprovalsRecord toRecord(StockAdjustmentApproval a) {
        if (a == null) return null;
        StockAdjustmentApprovalsRecord r = new StockAdjustmentApprovalsRecord();
        r.setId(a.getId());
        r.setProductId(a.getProductId());
        r.setWarehouseId(a.getWarehouseId());
        r.setLocationId(a.getLocationId());
        r.setStockBalanceId(a.getStockBalanceId());
        r.setQuantityAdjustment(a.getQuantityAdjustment());
        r.setReason(a.getReason());
        r.setReferenceNo(a.getReferenceNo());
        r.setCreatedBy(a.getCreatedBy());
        if (a.getCreatedAt() != null) {
            r.setCreatedAt(a.getCreatedAt().atOffset(ZoneOffset.UTC));
        }
        return r;
    }
}
