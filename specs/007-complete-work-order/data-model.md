# Data Model: Complete Work Order

## Work Order

| Field | Role in completion | Rules |
|-------|--------------------|-------|
| `id` | Completion target | Must exist and be locked for the full operation. |
| `planned_quantity` | Consumption denominator | Positive by existing Work Order invariant. |
| `work_order_status_id` | Lifecycle state | Must resolve to `IN_PROGRESS`; changes to `COMPLETED` only when the active configured transition exists. |
| `finished_product_id` | Output product | Used by both classified finished-goods lots. |

State transition:

```text
IN_PROGRESS -- active configured transition --> COMPLETED
```

## Completion Request

| Field | Validation | Purpose |
|-------|------------|---------|
| `actualQuantity` | Required, non-negative | Total production result. |
| `goodQuantity` | Required, non-negative | Good output classification. |
| `defectQuantity` | Required, non-negative | Defective output classification. |
| `scrapQuantity` | Required, non-negative | Production scrap used for material-loss allocation. |
| `outputWarehouseId` | Required existing warehouse | Destination for classified output. |
| `outputLocationId` | Required existing location in the warehouse | Destination location for classified output. |
| `note` | Optional, maximum 500 characters | Completion-event note. |

Invariant: `goodQuantity + defectQuantity + scrapQuantity = actualQuantity`.

## Production Run

| Field | Completion update |
|-------|-------------------|
| `end_time` | Set once at completion. |
| `actual_quantity` | Set to request actual quantity. |
| `good_quantity` | Set to request good quantity. |
| `defect_quantity` | Set to request defect quantity. |
| `scrap_quantity` | Set to request scrap quantity. |

Relationship: one active run belongs to the Work Order and identifies the machine returned to `AVAILABLE`.

## Work Order Material and Reservation Movements

For each material line:

```text
lineConsumed = min(reservedQuantity x actualQuantity / plannedQuantity, reservedQuantity)
lineReleased = reservedQuantity - lineConsumed
```

The system derives original allocations from `RESERVE` movements, net of earlier `RELEASE_RESERVATION` movements, by `(warehouse, location, material product, lot)`.

For each consumed allocation when actual quantity is non-zero:

```text
allocationScrap = allocationConsumed x scrapQuantity / actualQuantity
allocationNormal = allocationConsumed - allocationScrap
```

`allocationNormal` is recorded as `CONSUME_IN_PRODUCTION`; `allocationScrap` is recorded as `SCRAP`. Their sum is the sole deduction from that reserved balance. The outstanding allocation is returned through `RELEASE_RESERVATION`. The material line's `consumed_quantity` increases by `lineConsumed`; its remaining reserved quantity becomes zero.

## Finished-Goods Lots and Balances

| Classification | Lot | Stock balance | QC inspection | Movement |
|----------------|-----|---------------|---------------|----------|
| Good | New, unique production-output lot | Requested warehouse/location, `QUALITY_INSPECTION`, `goodQuantity` | Pending, `goodQuantity` | `PRODUCTION_OUTPUT` when quantity is positive |
| Defective | New, unique production-output lot | Requested warehouse/location, `QUALITY_INSPECTION`, `defectQuantity` | Pending, `defectQuantity` | `PRODUCTION_OUTPUT` when quantity is positive |
| Scrap | None | None | None | Per-material-lot `SCRAP` movement |

The good and defective lot identities must be distinct even if one quantity is zero. A reusable lot-number generator creates a new unique number for each output lot.

## Quality Inspection

| Field | Rule |
|-------|------|
| `work_order_id` | Completed Work Order. |
| `product_id` | Finished product. |
| `lot_id` | Corresponding good or defective output lot. |
| `quantity` | Matches classified lot balance; may be zero. |
| `qc_status_id` | `PENDING_INSPECTION`. |

The migration changes the inspection quantity check from strictly positive to non-negative.

## Event and Audit History

| Record | Completion content |
|--------|--------------------|
| Work Order event | `COMPLETE`, active run ID, Operator ID, current time, optional note. |
| Audit entry | Action `COMPLETE_PRODUCTION`, Work Order ID, `IN_PROGRESS -> COMPLETED` transition. |
