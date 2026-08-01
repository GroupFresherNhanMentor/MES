# Contract: Complete Work Order

## Endpoint

`POST /api/v1/work-orders/{id}/complete`

Completes one in-progress Work Order. Only authenticated users with the `OPERATOR` role may call it.

## Request

```json
{
  "actualQuantity": 100.0000,
  "goodQuantity": 90.0000,
  "defectQuantity": 5.0000,
  "scrapQuantity": 5.0000,
  "outputWarehouseId": "550e8400-e29b-41d4-a716-446655440000",
  "outputLocationId": "550e8400-e29b-41d4-a716-446655440001",
  "note": "Shift B completion"
}
```

| Field | Required | Rules |
|-------|----------|-------|
| `actualQuantity` | Yes | Decimal greater than or equal to zero. |
| `goodQuantity` | Yes | Decimal greater than or equal to zero. |
| `defectQuantity` | Yes | Decimal greater than or equal to zero. |
| `scrapQuantity` | Yes | Decimal greater than or equal to zero. |
| `outputWarehouseId` | Yes | Existing warehouse UUID. |
| `outputLocationId` | Yes | Existing location UUID belonging to `outputWarehouseId`. |
| `note` | No | At most 500 characters. |

`goodQuantity + defectQuantity + scrapQuantity` must equal `actualQuantity`.

## Successful Response

HTTP `200 OK`:

```json
{
  "success": true,
  "data": {
    "id": "550e8400-e29b-41d4-a716-446655440010",
    "workOrderStatusId": "550e8400-e29b-41d4-a716-446655440011"
  },
  "message": "Production completed successfully",
  "timestamp": "2026-08-01T10:30:00Z"
}
```

The response data follows the existing Work Order response contract and reflects the `COMPLETED` state.

## Failure Responses

| Scenario | Status | Error category |
|----------|--------|----------------|
| No valid authentication | 401 | Unauthorized |
| Authenticated user lacks `OPERATOR` | 403 | Forbidden |
| Work Order does not exist | 404 | Not found |
| Invalid/missing quantity or invalid sum | 400 | Invalid input |
| Invalid output warehouse/location | 400 | Invalid input |
| Work Order is not in progress, lacks active run, or transition is inactive | 400 | Invalid status transition/input |
| A concurrent request already finalized the order | 400 | Invalid status transition/input |

All failure responses use the platform API error envelope and make no partial completion changes.
