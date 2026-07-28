# Application Interface Contract: InventoryUseCase

**Module**: `fpt.qn.mes.inventory.application.port.in`

## Port Interface: `InventoryUseCase`

```java
package fpt.qn.mes.inventory.application.port.in;

import java.util.List;
import java.util.UUID;
import fpt.qn.mes.common.dto.response.PageResponse;
import fpt.qn.mes.inventory.application.dto.request.CreateMovementRequest;
import fpt.qn.mes.inventory.application.dto.request.CreateStockLotRequest;
import fpt.qn.mes.inventory.application.dto.response.StockBalanceDto;
import fpt.qn.mes.inventory.application.dto.response.StockLotDto;
import fpt.qn.mes.inventory.application.dto.response.StockMovementDto;

public interface InventoryUseCase {
    PageResponse<StockLotDto> getStockLots(int page, int size);
    StockLotDto getStockLotById(UUID id);
    StockLotDto createStockLot(CreateStockLotRequest request);
    PageResponse<StockMovementDto> getMovements(int page, int size);
    StockMovementDto recordMovement(CreateMovementRequest request, UUID currentUserId);
    List<StockBalanceDto> getStockBalances(UUID warehouseId, UUID productId);
}
```

## Contract Operations & Exceptions

### 1. `createStockLot`
- **Inputs**: `CreateStockLotRequest` (lotNumber, productId, lotTypeId, expiryDate)
- **Outputs**: `StockLotDto`
- **Exceptions**:
  - `AppException(HttpStatus.CONFLICT, "LOT_NUMBER_EXISTS", "Lot number already exists")`
  - `AppException(HttpStatus.BAD_REQUEST, "INVALID_PRODUCT", "Product ID invalid or missing")`

### 2. `recordMovement`
- **Inputs**: `CreateMovementRequest` (movementTypeId, productId, lotId, warehouseId, locationId, quantity, fromStatusId, toStatusId, referenceNo, reason), `UUID currentUserId`
- **Outputs**: `StockMovementDto`
- **Exceptions**:
  - `AppException(HttpStatus.BAD_REQUEST, "INSUFFICIENT_STOCK", "Stock balance is insufficient for this movement")`
  - `AppException(HttpStatus.NOT_FOUND, "LOT_NOT_FOUND", "Specified stock lot was not found")`
  - `AppException(HttpStatus.BAD_REQUEST, "INVALID_MOVEMENT_TYPE", "Unrecognized movement type")`

### 3. `getStockLotById`
- **Inputs**: `UUID id`
- **Outputs**: `StockLotDto`
- **Exceptions**:
  - `AppException(HttpStatus.NOT_FOUND, "STOCK_LOT_NOT_FOUND", "Stock lot not found with ID: " + id)`

### 4. `getStockBalances`
- **Inputs**: `UUID warehouseId`, `UUID productId`
- **Outputs**: `List<StockBalanceDto>`
- **Behavior**: Returns on-hand balances filtered by warehouse and product.

### 5. `getStockLots` / `getMovements`
- **Inputs**: `int page` (0-indexed), `int size` (> 0)
- **Outputs**: `PageResponse<StockLotDto>` / `PageResponse<StockMovementDto>`
- **Behavior**: Paginated results matching request parameters.
