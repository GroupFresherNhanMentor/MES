# Data Model & DTO Specs: Get BOM Statuses (Lookup)

## 1. Database Table (`bom_statuses`)

| Column | Type | Constraint | Description |
|---|---|---|---|
| `id` | `UUID` | Primary Key, Non-null | Unique status ID |
| `name` | `VARCHAR(50)` | Unique, Non-null | Status name (`DRAFT`, `ACTIVE`, `INACTIVE`) |
| `description` | `TEXT` | Nullable | Human-readable explanation of status |

## 2. DTO Specifications

### `LookupEntry` (`fpt.qn.mes.common.service.LookupEntry`)

```java
@Value
@Builder
public class LookupEntry {
    UUID id;
    String name;
    String description;
}
```

## 3. Seed Data Matrix

| Name | Description |
|---|---|
| `DRAFT` | Draft BOM version, can be edited |
| `ACTIVE` | Active BOM version used for production Work Orders |
| `INACTIVE` | Superseded or deactivated BOM version |
