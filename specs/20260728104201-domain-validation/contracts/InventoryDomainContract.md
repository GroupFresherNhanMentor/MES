# Domain Interface & Entity Contract: Inventory Domain Validation

**Module**: `fpt.qn.mes.inventory.domain.entities`

## Database Schema Source of Truth (`V20260726194837__init.sql`)
- Tables: `stock_statuses`, `lot_types`, `movement_types`
- Fields per table: `id` (UUID), `name` (VARCHAR 50 UNIQUE), `description` (VARCHAR 255)

---

## Pure Java Domain Value Objects

```java
package fpt.qn.mes.inventory.domain.entities;

import java.util.UUID;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class StockStatus {
    UUID id;
    String name;
    String description;
}
```

```java
package fpt.qn.mes.inventory.domain.entities;

import java.util.UUID;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class LotType {
    UUID id;
    String name;
    String description;
}
```

```java
package fpt.qn.mes.inventory.domain.entities;

import java.util.UUID;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class MovementType {
    UUID id;
    String name;
    String description;
}
```

---

## Domain Invariants & Entity Operations

```java
// StockBalance invariant method
public void deductQuantity(BigDecimal amount) {
    if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
        throw new IllegalArgumentException("Deduction quantity must be positive");
    }
    if (this.quantity.compareTo(amount) < 0) {
        throw new IllegalArgumentException("Insufficient stock balance");
    }
    this.quantity = this.quantity.subtract(amount);
}
```
