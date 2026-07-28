package fpt.qn.mes.inventory.domain.entities;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import fpt.qn.mes.common.exception.DomainException;

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
    UUID id;
    UUID movementTypeId;
    MovementType movementType;
    UUID productId;
    UUID lotId;
    UUID warehouseId;
    UUID locationId;
    BigDecimal quantity;
    UUID fromStatusId;
    UUID toStatusId;
    String referenceNo;
    String reason;
    UUID createdBy;
    Instant createdAt;

    public static StockMovement create(UUID movementTypeId, UUID productId, UUID lotId,
            UUID warehouseId, UUID locationId, BigDecimal quantity, UUID fromStatusId,
            UUID toStatusId, String referenceNo, String reason, UUID createdBy) {

        if (quantity == null || quantity.compareTo(BigDecimal.ZERO) <= 0) {
            throw new DomainException("Movement quantity must be strictly positive");
        }
        if (productId == null) {
            throw new DomainException("Product ID cannot be null");
        }
        if (warehouseId == null) {
            throw new DomainException("Warehouse ID cannot be null");
        }
        if (createdBy == null) {
            throw new DomainException("Created-by user ID cannot be null");
        }

        return StockMovement.builder()
                .id(UUID.randomUUID())
                .movementTypeId(movementTypeId)
                .productId(productId)
                .lotId(lotId)
                .warehouseId(warehouseId)
                .locationId(locationId)
                .quantity(quantity)
                .fromStatusId(fromStatusId)
                .toStatusId(toStatusId)
                .referenceNo(referenceNo)
                .reason(reason)
                .createdBy(createdBy)
                .createdAt(Instant.now())
                .build();
    }
}
