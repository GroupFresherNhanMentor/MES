# Research & Architectural Decisions: Stock Adjustments API

## Decision 1: Threshold Configuration and Evaluation Mechanism

- **Decision**: Define `@Value("${app.inventory.adjustment-threshold:100.00}") BigDecimal adjustmentThreshold` in Spring application configuration.
- **Rationale**: Keeps adjustment threshold configurable without code redeployment, while defaulting to a safe baseline value of `100.00`.
- **Evaluation Pattern**:
  ```java
  boolean requiresApproval = request.getQuantityAdjustment().abs().compareTo(adjustmentThreshold) > 0;
  ```
- **Alternatives Considered**:
  - Database dynamic configuration table: Rejected for v1 to avoid unnecessary query overhead on every adjustment request.

## Decision 2: Pending Adjustment Record Lifecycle & Deletion Pattern

- **Decision**: Pending threshold-exceeding adjustments are saved into `stock_adjustment_approvals` table. When Factory Manager approves or rejects an adjustment, the pending record is deleted from `stock_adjustment_approvals`.
- **Rationale**: Fulfills explicit user requirement ("if factory manager approve/deny it will be removed in db") avoiding state pollution in pending queue.
- **Transactional Boundary**:
  - **Approval (`POST /api/stock-adjustments/{id}/approve`)**: Executed inside `@Transactional`.
    1. Lock and fetch `StockAdjustmentApproval` record.
    2. Retrieve or initialize `StockBalance` with `FOR UPDATE` pessimistic row lock.
    3. Assert `balance.quantity + adjustment.quantityAdjustment >= 0`.
    4. Update `StockBalance` quantity.
    5. Log `StockMovement` with type `ADJUSTMENT`.
    6. Delete `StockAdjustmentApproval` record from DB.
  - **Rejection (`POST /api/stock-adjustments/{id}/reject`)**: Executed inside `@Transactional`.
    1. Fetch `StockAdjustmentApproval` record.
    2. Delete `StockAdjustmentApproval` record from DB.
- **Alternatives Considered**:
  - Soft-delete or keeping `status = APPROVED/REJECTED`: Explicitly rejected by user directive.

## Decision 3: Non-Negative Quantity Guard & Concurrency Control

- **Decision**: Combine explicit pre-update assertion in service layer with jOOQ SQL row locking (`FOR UPDATE`) on `stock_balances`.
- **Rationale**: Prevents race conditions where concurrent adjustments could bypass the `resultingQuantity >= 0` check.
- **Error Handling**: Throws `AppException(HttpStatus.BAD_REQUEST, "INVALID_ADJUSTMENT_QUANTITY", "Stock balance cannot be negative")` if validation fails.
