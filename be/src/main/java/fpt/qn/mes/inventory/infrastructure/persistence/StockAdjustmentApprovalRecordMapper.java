package fpt.qn.mes.inventory.infrastructure.persistence;

import java.time.ZoneOffset;

import org.springframework.stereotype.Component;

import fpt.qn.mes.inventory.domain.entities.StockAdjustmentApproval;
import fpt.qn.mes.jooq.tables.records.ProductsRecord;
import fpt.qn.mes.jooq.tables.records.StockAdjustmentApprovalsRecord;
import fpt.qn.mes.jooq.tables.records.UsersRecord;
import fpt.qn.mes.jooq.tables.records.WarehouseLocationsRecord;
import fpt.qn.mes.jooq.tables.records.WarehousesRecord;

@Component
public class StockAdjustmentApprovalRecordMapper {

    /** Plain mapping — write path (findById used by approve/reject). Only IDs are known. */
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

    /** Enriched mapping — read path (search with JOINs). */
    public StockAdjustmentApproval toDomain(StockAdjustmentApprovalsRecord ap,
            ProductsRecord p, WarehousesRecord w,
            WarehouseLocationsRecord l, UsersRecord creator) {
        return StockAdjustmentApproval.builder()
                .id(ap.getId())
                .productId(ap.getProductId())
                .warehouseId(ap.getWarehouseId())
                .locationId(ap.getLocationId())
                .stockBalanceId(ap.getStockBalanceId())
                .quantityAdjustment(ap.getQuantityAdjustment())
                .reason(ap.getReason())
                .referenceNo(ap.getReferenceNo())
                .createdBy(ap.getCreatedBy())
                .createdAt(ap.getCreatedAt() != null ? ap.getCreatedAt().toInstant() : null)
                .product(p.getId() != null
                        ? StockAdjustmentApproval.ProductRef.builder()
                                .id(p.getId()).code(p.getCode()).name(p.getName()).build()
                        : null)
                .warehouse(w.getId() != null
                        ? StockAdjustmentApproval.WarehouseRef.builder()
                                .id(w.getId()).code(w.getCode()).name(w.getName()).build()
                        : null)
                .location(l.getId() != null
                        ? StockAdjustmentApproval.WarehouseLocationRef.builder()
                                .id(l.getId()).code(l.getCode()).name(l.getName()).build()
                        : null)
                .createdByUser(creator.getId() != null
                        ? StockAdjustmentApproval.UserRef.builder()
                                .id(creator.getId())
                                .username(creator.getUsername())
                                .fullName(creator.getFullName())
                                .build()
                        : null)
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
