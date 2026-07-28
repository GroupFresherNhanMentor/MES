# Domain Interface Contract: Inventory Search & PageResponse Refactoring

**Package**: `fpt.qn.mes.inventory.domain.repository`

## Contract Signatures

### 1. `BaseSearchCriteria.java` (Pure Java Base Class)
```java
package fpt.qn.mes.common.dto;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public abstract class BaseSearchCriteria {
    @SuperBuilder.Default
    int page = 0;

    @SuperBuilder.Default
    int size = 20;
}
```

### 2. `StockBalanceSearchCriteria.java` (Domain Criteria)
```java
package fpt.qn.mes.inventory.domain.repository;

import fpt.qn.mes.common.dto.BaseSearchCriteria;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class StockBalanceSearchCriteria extends BaseSearchCriteria {
    UUID warehouseId;
    UUID locationId;
    UUID productId;
    UUID lotId;
    UUID stockStatusId;
}
```

### 3. `StockLotSearchCriteria.java` (Domain Criteria)
```java
package fpt.qn.mes.inventory.domain.repository;

import fpt.qn.mes.common.dto.BaseSearchCriteria;
import java.time.LocalDate;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class StockLotSearchCriteria extends BaseSearchCriteria {
    UUID productId;
    UUID lotTypeId;
    String lotNumber;
    LocalDate expiryBefore;
}
```

### 4. `StockMovementSearchCriteria.java` (Domain Criteria)
```java
package fpt.qn.mes.inventory.domain.repository;

import fpt.qn.mes.common.dto.BaseSearchCriteria;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class StockMovementSearchCriteria extends BaseSearchCriteria {
    UUID movementTypeId;
    UUID productId;
    UUID lotId;
    UUID warehouseId;
    UUID locationId;
    String referenceNo;
}
```

### 4. Application Search Request DTOs (`application/dto/request/`)
```java
package fpt.qn.mes.inventory.application.dto.request;

import fpt.qn.mes.common.dto.request.PageRequest;
import java.time.LocalDate;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class StockLotSearchRequest extends PageRequest {
    UUID productId;
    UUID lotTypeId;
    String lotNumber;
    LocalDate expiryBefore;
}
```

### 4. `StockBalanceRepository.java`
```java
package fpt.qn.mes.inventory.domain.repository;

import fpt.qn.mes.common.dto.response.PageResponse;
import fpt.qn.mes.inventory.domain.entities.StockBalance;

public interface StockBalanceRepository {
    PageResponse<StockBalance> search(StockBalanceSearchCriteria criteria);
}
```

### 5. `StockLotRepository.java`
```java
package fpt.qn.mes.inventory.domain.repository;

import fpt.qn.mes.common.dto.response.PageResponse;
import fpt.qn.mes.inventory.domain.entities.StockLot;

public interface StockLotRepository {
    PageResponse<StockLot> search(StockLotSearchCriteria criteria);
}
```

### 6. `StockMovementRepository.java`
```java
package fpt.qn.mes.inventory.domain.repository;

import fpt.qn.mes.common.dto.response.PageResponse;
import fpt.qn.mes.inventory.domain.entities.StockMovement;

public interface StockMovementRepository {
    PageResponse<StockMovement> search(StockMovementSearchCriteria criteria);
}
```
