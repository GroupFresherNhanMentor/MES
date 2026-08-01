package fpt.qn.mes.inventory.domain.entities;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import fpt.qn.mes.common.exception.DomainException;
import fpt.qn.mes.common.util.UuidV7;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@FieldDefaults(level = AccessLevel.PRIVATE)
public class StockMovement {

    @Getter @Builder @NoArgsConstructor @AllArgsConstructor
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class ProductRef { UUID id; String code; String name; }

    @Getter @Builder @NoArgsConstructor @AllArgsConstructor
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class WarehouseRef { UUID id; String code; String name; }

    @Getter @Builder @NoArgsConstructor @AllArgsConstructor
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class WarehouseLocationRef { UUID id; String code; String name; }

    @Getter @Builder @NoArgsConstructor @AllArgsConstructor
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class UserRef { UUID id; String username; String fullName; }

    UUID id;
    MovementType movementType;
    StockLot stockLot;
    StockStatus fromStatus;
    StockStatus toStatus;

    UUID productId;
    UUID fromWarehouseId;
    UUID fromLocationId;
    UUID toWarehouseId;
    UUID toLocationId;
    BigDecimal quantity;
    String referenceNo;
    String reason;
    UUID createdBy;
    Instant createdAt;

    ProductRef product;
    WarehouseRef fromWarehouse;
    WarehouseRef toWarehouse;
    WarehouseLocationRef fromLocation;
    WarehouseLocationRef toLocation;
    UserRef createdByUser;

    public static StockMovement create(
            UUID movementTypeId,
            UUID productId,
            UUID lotId,
            UUID fromWarehouseId,
            UUID fromLocationId,
            UUID toWarehouseId,
            UUID toLocationId,
            BigDecimal quantity,
            UUID fromStatusId,
            UUID toStatusId,
            String referenceNo,
            String reason,
            UUID createdBy) {

        if (quantity == null || quantity.compareTo(BigDecimal.ZERO) <= 0) {
            throw new DomainException("Movement quantity must be strictly positive");
        }
        if (productId == null) {
            throw new DomainException("Product ID cannot be null");
        }
        // if (toWarehouseId == null) {
        //     throw new DomainException("At least one warehouse (fromWarehouseId or toWarehouseId) must be specified");
        // }
        if (createdBy == null) {
            throw new DomainException("Created-by user ID cannot be null");
        }

        MovementType movementType = movementTypeId != null ? MovementType.builder().id(movementTypeId).build() : null;
        StockLot stockLot = lotId != null ? StockLot.builder().id(lotId).build() : null;
        StockStatus fromStatus = fromStatusId != null ? StockStatus.builder().id(fromStatusId).build() : null;
        StockStatus toStatus = toStatusId != null ? StockStatus.builder().id(toStatusId).build() : null;

        return StockMovement.builder()
                .id(UuidV7.generate())
                .movementType(movementType)
                .productId(productId)
                .stockLot(stockLot)
                .fromWarehouseId(fromWarehouseId)
                .fromLocationId(fromLocationId)
                .toWarehouseId(toWarehouseId)
                .toLocationId(toLocationId)
                .quantity(quantity)
                .fromStatus(fromStatus)
                .toStatus(toStatus)
                .referenceNo(referenceNo)
                .reason(reason)
                .createdBy(createdBy)
                .createdAt(Instant.now())
                .build();
    }

    public UUID getWarehouseId() {
        return toWarehouseId != null ? toWarehouseId : fromWarehouseId;
    }

    public UUID getLocationId() {
        return toLocationId != null ? toLocationId : fromLocationId;
    }

    public UUID getMovementTypeId() {
        return movementType != null ? movementType.getId() : null;
    }

    public UUID getLotId() {
        return stockLot != null ? stockLot.getId() : null;
    }

    public UUID getFromStatusId() {
        return fromStatus != null ? fromStatus.getId() : null;
    }

    public UUID getToStatusId() {
        return toStatus != null ? toStatus.getId() : null;
    }
}
