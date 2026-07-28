package fpt.qn.mes.inventory.domain.entities;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

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
public class StockBalance {
    UUID id;
    UUID warehouseId;
    UUID locationId;
    UUID productId;
    UUID lotId;
    UUID stockStatusId;
    StockStatus stockStatus;
    BigDecimal quantity;
    Long version;
    Instant createdAt;
    Instant updatedAt;

    public void deductQuantity(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Deduction quantity must be positive");
        }
        if (this.quantity == null || this.quantity.compareTo(amount) < 0) {
            throw new IllegalArgumentException("Insufficient stock balance for deduction");
        }
        this.quantity = this.quantity.subtract(amount);
    }

    public void addQuantity(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Addition quantity must be positive");
        }
        if (this.quantity == null) {
            this.quantity = amount;
        } else {
            this.quantity = this.quantity.add(amount);
        }
    }
}
