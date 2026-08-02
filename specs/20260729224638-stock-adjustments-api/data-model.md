# Data Model & Schema Specification: Stock Adjustments

## 1. Database Table: `stock_adjustment_approvals`

### Migration SQL (`be/src/main/resources/db/migration/V20260729224500__add_stock_adjustment_approvals.sql`)

```sql
CREATE TABLE stock_adjustment_approvals (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    product_id UUID NOT NULL REFERENCES products(id),
    warehouse_id UUID NOT NULL REFERENCES warehouses(id),
    location_id UUID REFERENCES warehouse_locations(id),
    stock_balance_id UUID REFERENCES stock_balances(id) ON DELETE CASCADE,
    quantity_adjustment NUMERIC(15, 4) NOT NULL,
    reason TEXT NOT NULL,
    reference_no VARCHAR(100),
    created_by UUID NOT NULL REFERENCES users(id),
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_stock_adjustment_approvals_product ON stock_adjustment_approvals(product_id);
CREATE INDEX idx_stock_adjustment_approvals_warehouse ON stock_adjustment_approvals(warehouse_id);
CREATE INDEX idx_stock_adjustment_approvals_balance ON stock_adjustment_approvals(stock_balance_id);
CREATE INDEX idx_stock_adjustment_approvals_created_by ON stock_adjustment_approvals(created_by);
```

## 2. Domain Entity: `StockAdjustmentApproval`

**Package**: `fpt.qn.mes.inventory.domain.entities`

```java
package fpt.qn.mes.inventory.domain.entities;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.experimental.FieldDefaults;

@Getter
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class StockAdjustmentApproval {
    UUID id;
    UUID productId;
    UUID warehouseId;
    UUID locationId;
    UUID stockBalanceId;
    BigDecimal quantityAdjustment;
    String reason;
    String referenceNo;
    UUID createdBy;
    Instant createdAt;

    public static StockAdjustmentApproval create(
            UUID productId,
            UUID warehouseId,
            UUID locationId,
            UUID stockBalanceId,
            BigDecimal quantityAdjustment,
            String reason,
            String referenceNo,
            UUID createdBy) {
        return StockAdjustmentApproval.builder()
                .productId(productId)
                .warehouseId(warehouseId)
                .locationId(locationId)
                .stockBalanceId(stockBalanceId)
                .quantityAdjustment(quantityAdjustment)
                .reason(reason)
                .referenceNo(referenceNo)
                .createdBy(createdBy)
                .createdAt(Instant.now())
                .build();
    }
}
```

## 3. Domain Entity Invariants & Modifications

### `StockBalance` Invariants
- `quantity`: Must be `BigDecimal` $\ge 0$.
- `resultingQuantity = currentQuantity + quantityAdjustment`.
- If `resultingQuantity < 0`, system rejects adjustment with HTTP 400 Bad Request ("Stock balance cannot be negative").

### `StockMovement` Integration
- Ledger Movement Type: `ADJUSTMENT`.
- `quantity`: `quantityAdjustment.abs()`.
- `reason`: Mandatory non-blank string provided in adjustment request payload.
