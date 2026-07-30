package fpt.qn.mes.inventory.infrastructure.persistence;

import java.time.ZoneOffset;

import org.springframework.stereotype.Component;
import fpt.qn.mes.inventory.domain.entities.LotType;
import fpt.qn.mes.inventory.domain.entities.StockBalance;
import fpt.qn.mes.inventory.domain.entities.StockLot;
import fpt.qn.mes.inventory.domain.entities.StockMovement;
import fpt.qn.mes.jooq.tables.records.StockBalancesRecord;
import fpt.qn.mes.jooq.tables.records.StockLotsRecord;
import fpt.qn.mes.jooq.tables.records.StockMovementsRecord;

@Component
public class InventoryRecordMapper {

    public StockLot toDomain(StockLotsRecord r) {
        if (r == null) return null;
        return StockLot.builder()
                .id(r.getId())
                .lotNumber(r.getLotNumber())
                .productId(r.getProductId())
                .lotType(LotType.builder().id(r.getLotTypeId()).build())
                .expiryDate(r.getExpiryDate())
                .createdAt(r.getCreatedAt() != null ? r.getCreatedAt().toInstant() : null)
                .build();
    }

    public StockLotsRecord toRecord(StockLot l) {
        if (l == null) return null;
        StockLotsRecord r = new StockLotsRecord();
        r.setId(l.getId());
        r.setLotNumber(l.getLotNumber());
        r.setProductId(l.getProductId());
        r.setLotTypeId(l.getLotType().getId());
        r.setExpiryDate(l.getExpiryDate());
        if (l.getCreatedAt() != null) {
            r.setCreatedAt(l.getCreatedAt().atOffset(ZoneOffset.UTC));
        }
        return r;
    }

    public StockMovement toDomain(StockMovementsRecord r) {
        if (r == null) return null;
        return StockMovement.builder()
                .id(r.getId())
                .movementType(r.getMovementTypeId() != null ? fpt.qn.mes.inventory.domain.entities.MovementType.builder().id(r.getMovementTypeId()).build() : null)
                .productId(r.getProductId())
                .workOrderId(r.getWorkOrderId())
                .stockLot(r.getLotId() != null ? fpt.qn.mes.inventory.domain.entities.StockLot.builder().id(r.getLotId()).build() : null)
                .fromWarehouseId(r.getFromWarehouseId())
                .fromLocationId(r.getFromLocationId())
                .toWarehouseId(r.getToWarehouseId())
                .toLocationId(r.getToLocationId())
                .quantity(r.getQuantity())
                .fromStatus(r.getFromStatusId() != null ? fpt.qn.mes.inventory.domain.entities.StockStatus.builder().id(r.getFromStatusId()).build() : null)
                .toStatus(r.getToStatusId() != null ? fpt.qn.mes.inventory.domain.entities.StockStatus.builder().id(r.getToStatusId()).build() : null)
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

    public StockBalance toDomain(StockBalancesRecord r) {
        if (r == null) return null;
        return StockBalance.builder()
                .id(r.getId())
                .warehouseId(r.getWarehouseId())
                .locationId(r.getLocationId())
                .productId(r.getProductId())
                .lotId(r.getLotId())
                .stockStatusId(r.getStockStatusId())
                .quantity(r.getQuantity())
                .version(r.getVersion())
                .createdAt(r.getCreatedAt() != null ? r.getCreatedAt().toInstant() : null)
                .updatedAt(r.getUpdatedAt() != null ? r.getUpdatedAt().toInstant() : null)
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
