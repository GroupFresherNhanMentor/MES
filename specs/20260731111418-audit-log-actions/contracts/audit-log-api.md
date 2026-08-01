# REST API Contract: Audit Log (`/api/audit-logs`)

## Base Path
`/api/audit-logs`

## Security Requirements
- Requires JWT Bearer Authentication (`Authorization: Bearer <token>`).
- Requires role `ROLE_ADMIN`, `ROLE_AUDITOR`, or `ROLE_FACTORY_MANAGER`.

---

## 1. Search Audit Logs

### Request
`GET /api/audit-logs`

### Query Parameters
| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `entityType` | `string` | No | Filter by entity type (e.g. `WORK_ORDER`, `STOCK_BALANCE`, `BOM`) |
| `entityId` | `UUID` | No | Filter by target entity ID |
| `actorId` | `UUID` | No | Filter by actor user ID |
| `action` | `string` | No | Filter by audit action enum name |
| `from` | `ISO-8601 String` | No | Filter created timestamp >= from |
| `to` | `ISO-8601 String` | No | Filter created timestamp <= to |
| `page` | `integer` | No (default: 0) | Page index (0-indexed) |
| `size` | `integer` | No (default: 20) | Page size |
| `sort` | `list` | No | Field sort (e.g. `createdAt,desc`) |

### Response (`200 OK`)
```json
{
  "success": true,
  "data": {
    "items": [
      {
        "id": "019fb7a2-1234-7000-8000-000000000001",
        "actorId": "019fb6c7-292a-79d0-9124-5ee0715ef474",
        "action": "CREATE_WORK_ORDER",
        "entityType": "WORK_ORDER",
        "entityId": "019fb7a1-8888-7000-8000-000000000099",
        "oldValue": null,
        "newValue": "{\"code\":\"WO-2026-001\",\"quantity\":100,\"status\":\"PLANNED\"}",
        "ipAddress": "127.0.0.1",
        "createdAt": "2026-07-31T14:30:00Z"
      }
    ],
    "totalElements": 1,
    "totalPages": 1,
    "pageNumber": 0,
    "pageSize": 20
  },
  "message": "OK"
}
```

---

## 2. Get Audit Log Detail By ID

### Request
`GET /api/audit-logs/{id}`

### Response (`200 OK`)
```json
{
  "success": true,
  "data": {
    "id": "019fb7a2-1234-7000-8000-000000000001",
    "actorId": "019fb6c7-292a-79d0-9124-5ee0715ef474",
    "action": "CREATE_WORK_ORDER",
    "entityType": "WORK_ORDER",
    "entityId": "019fb7a1-8888-7000-8000-000000000099",
    "oldValue": null,
    "newValue": "{\"code\":\"WO-2026-001\",\"quantity\":100,\"status\":\"PLANNED\"}",
    "ipAddress": "127.0.0.1",
    "createdAt": "2026-07-31T14:30:00Z"
  },
  "message": "OK"
}
```

---

## Error Responses

- `401 Unauthorized`: Token missing or invalid.
- `403 Forbidden`: User lacks necessary roles.
- `404 Not Found`: Audit log record not found by ID.
