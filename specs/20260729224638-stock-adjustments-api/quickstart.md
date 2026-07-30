# Quickstart Validation Guide: Stock Adjustments & Approval Workflow

## Prerequisites & Test Execution

Run unit and slice test suites:
```bash
cd be
./mvnw test -Djooq.codegen.skip=true -Dtest="StockAdjustmentTest,StockAdjustmentServiceTest,StockAdjustmentControllerTest"
```

Run integration test suite:
```bash
./mvnw test -Dtest="StockAdjustmentIntegrationTest"
```

## Runnable Validation Scenarios

### Scenario 1: Standard Stock Adjustment Within Threshold
1. Send `POST /api/stock-adjustments` with:
   - `quantityAdjustment`: `50.00`
   - `reason`: `"Physical cycle count discrepancy correction"`
2. Verify response status is `201 Created` and `requiresApproval` is `false`.
3. Verify `stock_balances` quantity increased by `50.00`.
4. Verify `stock_movements` contains entry with type `ADJUSTMENT` and `reason`.

### Scenario 2: Negative Quantity Prevention Guard
1. Send `POST /api/stock-adjustments` for a location with 10 on-hand units with:
   - `quantityAdjustment`: `-15.00`
   - `reason`: `"Damaged goods write-off"`
2. Verify response status is `400 Bad Request` with error `"Stock balance cannot be negative"`.
3. Verify `stock_balances` remains unchanged at `10`.

### Scenario 3: Threshold Exceeded & Factory Manager Approval
1. Send `POST /api/stock-adjustments` with `quantityAdjustment`: `150.00` (threshold = `100.00`).
2. Verify response status is `201 Created` and `requiresApproval` is `true`.
3. Verify `stock_balances` is **NOT** updated immediately.
4. Verify row created in `stock_adjustment_approvals`.
5. Call `POST /api/stock-adjustments/{id}/approve` as Factory Manager (`ROLE_FACTORY_MANAGER`).
6. Verify status `200 OK`.
7. Verify `stock_balances` quantity increased by `150.00`, movement logged in `stock_movements`, and row **deleted** from `stock_adjustment_approvals`.
