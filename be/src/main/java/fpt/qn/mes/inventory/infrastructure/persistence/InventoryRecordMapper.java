package fpt.qn.mes.inventory.infrastructure.persistence;

import java.time.ZoneOffset;

import org.springframework.stereotype.Component;

import fpt.qn.mes.inventory.domain.entities.MovementType;
import fpt.qn.mes.inventory.domain.entities.StockBalance;
import fpt.qn.mes.inventory.domain.entities.StockLot;
import fpt.qn.mes.inventory.domain.entities.StockMovement;
import fpt.qn.mes.inventory.domain.entities.StockStatus;
import fpt.qn.mes.jooq.tables.records.MovementTypesRecord;
import fpt.qn.mes.jooq.tables.records.ProductsRecord;
import fpt.qn.mes.jooq.tables.records.StockBalancesRecord;
import fpt.qn.mes.jooq.tables.records.StockLotsRecord;
import fpt.qn.mes.jooq.tables.records.StockMovementsRecord;
import fpt.qn.mes.jooq.tables.records.StockStatusesRecord;
import fpt.qn.mes.jooq.tables.records.UsersRecord;
import fpt.qn.mes.jooq.tables.records.WarehouseLocationsRecord;
import fpt.qn.mes.jooq.tables.records.WarehousesRecord;

@Component
public class InventoryRecordMapper {

    public StockMovement toDomain(StockMovementsRecord r) {
        if (r == null) return null;
        return StockMovement.builder()
                .id(r.getId())
                .movementType(r.getMovementTypeId() != null ? MovementType.builder().id(r.getMovementTypeId()).build() : null)
                .productId(r.getProductId())
                .workOrderId(r.getWorkOrderId())
                .stockLot(r.getLotId() != null ? StockLot.builder().id(r.getLotId()).build() : null)
                .fromWarehouseId(r.getFromWarehouseId())
                .fromLocationId(r.getFromLocationId())
                .toWarehouseId(r.getToWarehouseId())
                .toLocationId(r.getToLocationId())
                .quantity(r.getQuantity())
                .fromStatus(r.getFromStatusId() != null ? StockStatus.builder().id(r.getFromStatusId()).build() : null)
                .toStatus(r.getToStatusId() != null ? StockStatus.builder().id(r.getToStatusId()).build() : null)
                .referenceNo(r.getReferenceNo())
                .reason(r.getReason())
                .createdBy(r.getCreatedBy())
                .createdAt(r.getCreatedAt() != null ? r.getCreatedAt().toInstant() : null)
                .build();
    }

    public StockMovementsRecord toRecord(StockMovement m) {
        if (m == null) return null;
        StockMovementsRecord r = new StockMovementsRecord();
        r.setId(m.getId());
        r.setMovementTypeId(m.getMovementTypeId());
        r.setProductId(m.getProductId());
        r.setLotId(m.getLotId());
        r.setWorkOrderId(m.getWorkOrderId());
        r.setFromWarehouseId(m.getFromWarehouseId());
        r.setFromLocationId(m.getFromLocationId());
        r.setToWarehouseId(m.getToWarehouseId());
        r.setToLocationId(m.getToLocationId());
        r.setQuantity(m.getQuantity());
        r.setFromStatusId(m.getFromStatusId());
        r.setToStatusId(m.getToStatusId());
        r.setReferenceNo(m.getReferenceNo());
        r.setReason(m.getReason());
        r.setCreatedBy(m.getCreatedBy());
        if (m.getCreatedAt() != null) {
            r.setCreatedAt(m.getCreatedAt().atOffset(ZoneOffset.UTC));
        }
        return r;
    }

    /** Plain mapping — used for write operations (findForUpdate → save). Only IDs are known. */
    public StockBalance toDomain(StockBalancesRecord r) {
        if (r == null) return null;
        return StockBalance.builder()
                .id(r.getId())
                .warehouse(StockBalance.WarehouseRef.builder().id(r.getWarehouseId()).build())
                .location(r.getLocationId() != null
                        ? StockBalance.LocationRef.builder().id(r.getLocationId()).build() : null)
                .product(StockBalance.ProductRef.builder().id(r.getProductId()).build())
                .lot(r.getLotId() != null
                        ? StockBalance.LotRef.builder().id(r.getLotId()).build() : null)
                .stockStatus(r.getStockStatusId() != null
                        ? StockBalance.StockStatusRef.builder().id(r.getStockStatusId()).build() : null)
                .quantity(r.getQuantity())
                .version(r.getVersion())
                .createdAt(r.getCreatedAt() != null ? r.getCreatedAt().toInstant() : null)
                .updatedAt(r.getUpdatedAt() != null ? r.getUpdatedAt().toInstant() : null)
                .build();
    }

    /** Enriched mapping — used for search reads with JOINs. */
    public StockBalance toDomain(StockBalancesRecord r, WarehousesRecord w,
            WarehouseLocationsRecord l, ProductsRecord p,
            StockLotsRecord sl, StockStatusesRecord ss) {
        return StockBalance.builder()
                .id(r.getId())
                .warehouse(w.getId() != null
                        ? StockBalance.WarehouseRef.builder()
                                .id(w.getId()).code(w.getCode()).name(w.getName()).build()
                        : StockBalance.WarehouseRef.builder().id(r.getWarehouseId()).build())
                .location(l.getId() != null
                        ? StockBalance.LocationRef.builder()
                                .id(l.getId()).code(l.getCode()).name(l.getName()).build()
                        : null)
                .product(p.getId() != null
                        ? StockBalance.ProductRef.builder()
                                .id(p.getId()).code(p.getCode()).name(p.getName()).build()
                        : StockBalance.ProductRef.builder().id(r.getProductId()).build())
                .lot(sl.getId() != null
                        ? StockBalance.LotRef.builder()
                                .id(sl.getId()).lotNumber(sl.getLotNumber()).build()
                        : null)
                .stockStatus(ss.getId() != null
                        ? StockBalance.StockStatusRef.builder()
                                .id(ss.getId()).name(ss.getName()).build()
                        : null)
                .quantity(r.getQuantity())
                .version(r.getVersion())
                .createdAt(r.getCreatedAt() != null ? r.getCreatedAt().toInstant() : null)
                .updatedAt(r.getUpdatedAt() != null ? r.getUpdatedAt().toInstant() : null)
                .build();
    }

    /** Enriched mapping — used for search/findById reads with JOINs. */
    public StockMovement toDomain(StockMovementsRecord sm, MovementTypesRecord mt,
            StockLotsRecord sl, StockStatusesRecord fs, StockStatusesRecord ts,
            ProductsRecord p, WarehousesRecord fromWh, WarehousesRecord toWh,
            WarehouseLocationsRecord fromLoc, WarehouseLocationsRecord toLoc,
            UsersRecord creator) {
        return StockMovement.builder()
                .id(sm.getId())
                .movementType(mt.getId() != null
                        ? MovementType.builder().id(mt.getId()).name(mt.getName()).build() : null)
                .productId(sm.getProductId())
                .workOrderId(sm.getWorkOrderId())
                .stockLot(sl.getId() != null
                        ? StockLot.builder().id(sl.getId()).lotNumber(sl.getLotNumber()).build() : null)
                .fromWarehouseId(sm.getFromWarehouseId())
                .fromLocationId(sm.getFromLocationId())
                .toWarehouseId(sm.getToWarehouseId())
                .toLocationId(sm.getToLocationId())
                .quantity(sm.getQuantity())
                .fromStatus(fs.getId() != null
                        ? StockStatus.builder().id(fs.getId()).name(fs.getName()).build() : null)
                .toStatus(ts.getId() != null
                        ? StockStatus.builder().id(ts.getId()).name(ts.getName()).build() : null)
                .referenceNo(sm.getReferenceNo())
                .reason(sm.getReason())
                .createdBy(sm.getCreatedBy())
                .createdAt(sm.getCreatedAt() != null ? sm.getCreatedAt().toInstant() : null)
                .product(p.getId() != null
                        ? StockMovement.ProductRef.builder()
                                .id(p.getId()).code(p.getCode()).name(p.getName()).build() : null)
                .fromWarehouse(fromWh.getId() != null
                        ? StockMovement.WarehouseRef.builder()
                                .id(fromWh.getId()).code(fromWh.getCode()).name(fromWh.getName()).build() : null)
                .toWarehouse(toWh.getId() != null
                        ? StockMovement.WarehouseRef.builder()
                                .id(toWh.getId()).code(toWh.getCode()).name(toWh.getName()).build() : null)
                .fromLocation(fromLoc.getId() != null
                        ? StockMovement.WarehouseLocationRef.builder()
                                .id(fromLoc.getId()).code(fromLoc.getCode()).name(fromLoc.getName()).build() : null)
                .toLocation(toLoc.getId() != null
                        ? StockMovement.WarehouseLocationRef.builder()
                                .id(toLoc.getId()).code(toLoc.getCode()).name(toLoc.getName()).build() : null)
                .createdByUser(creator.getId() != null
                        ? StockMovement.UserRef.builder()
                                .id(creator.getId())
                                .username(creator.getUsername())
                                .fullName(creator.getFullName())
                                .build() : null)
                .build();
    }

    public StockBalancesRecord toRecord(StockBalance b) {
        if (b == null) return null;
        StockBalancesRecord r = new StockBalancesRecord();
        r.setId(b.getId());
        r.setWarehouseId(b.getWarehouseId());
        r.setLocationId(b.getLocationId());
        r.setProductId(b.getProductId());
        r.setLotId(b.getLotId());
        r.setStockStatusId(b.getStockStatusId());
        r.setQuantity(b.getQuantity());
        r.setVersion(b.getVersion());
        if (b.getCreatedAt() != null) {
            r.setCreatedAt(b.getCreatedAt().atOffset(ZoneOffset.UTC));
        }
        if (b.getUpdatedAt() != null) {
            r.setUpdatedAt(b.getUpdatedAt().atOffset(ZoneOffset.UTC));
        }
        return r;
    }
}
