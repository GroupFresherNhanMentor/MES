package fpt.qn.mes.inventory.domain.entities;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import fpt.qn.mes.common.exception.DomainException;
import fpt.qn.mes.inventory.application.exception.InsufficientStockException;

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
    BigDecimal quantity;
    Long version;
    Instant createdAt;
    Instant updatedAt;

    public void deductQuantity(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new DomainException("Deduction quantity must be positive");
        }
        if (this.quantity == null || this.quantity.compareTo(amount) < 0) {
            throw new InsufficientStockException("Insufficient stock balance for deduction");
        }
        this.quantity = this.quantity.subtract(amount);
        this.version = this.version != null ? this.version + 1 : 1L;
    }

    public void addQuantity(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new DomainException("Addition quantity must be positive");
        }
        if (this.quantity == null) {
            this.quantity = amount;
        } else {
            this.quantity = this.quantity.add(amount);
        }

        this.version = this.version != null ? this.version + 1 : 1L;
    }
}
