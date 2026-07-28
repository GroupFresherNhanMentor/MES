# Data Model & Domain Rules: Add and Remove BOM Items

## 1. Domain Entities

### `BomItem`

Representing a component material item line within a Bill of Materials.

| Field | Type | Constraint | Description |
|---|---|---|---|
| `id` | `UUID` | Primary Key, Non-null | Unique identifier of the BOM item |
| `bomId` | `UUID` | Foreign Key (`boms.id`), Non-null | Target BOM header ID |
| `materialProductId` | `UUID` | Foreign Key (`products.id`), Non-null | Component product ID |
| `quantityPerUnit` | `BigDecimal` | Non-null, > 0 | Required quantity per 1 unit of finished product |
| `unit` | `String` | Non-null, Max 20 chars | Unit of measurement (e.g. `PCS`, `KG`, `METER`) |
| `scrapRate` | `BigDecimal` | Non-null, Default `0.00` | Allowance percentage for material loss/scrap |

## 2. DTO Specifications

### `CreateBomItemRequest`

```java
public class CreateBomItemRequest {
    @NotNull(message = "Material product ID is required")
    private UUID materialProductId;

    @NotNull(message = "Quantity per unit is required")
    @Positive(message = "Quantity per unit must be greater than zero")
    private BigDecimal quantityPerUnit;

    @NotBlank(message = "Unit is required")
    private String unit;

    private BigDecimal scrapRate; // Optional, defaults to 0.00
}
```

### `BomItemDto`

```java
public class BomItemDto {
    private UUID id;
    private UUID bomId;
    private UUID materialProductId;
    private BigDecimal quantityPerUnit;
    private String unit;
    private BigDecimal scrapRate;
}
```

## 3. Immutability Matrix

| BOM Status | Can Add Item? | Can Remove Item? | Error Thrown if Modified |
|---|---|---|---|
| `DRAFT` | Yes | Yes | None |
| `ACTIVE` | No | No | `InvalidBomStatusException` (HTTP 400) |
| `INACTIVE` | No | No | `InvalidBomStatusException` (HTTP 400) |
