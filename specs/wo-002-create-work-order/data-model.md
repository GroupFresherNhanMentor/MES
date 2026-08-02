# Data Model & Contracts: Create Work Order (`POST /api/work-orders`)

## 1. Domain Entities

### `WorkOrder`
```java
public class WorkOrder {
    UUID id;
    String code;
    UUID finishedProductId;
    UUID bomId;
    BigDecimal plannedQuantity;
    Instant plannedStartDate;
    Instant plannedEndDate;
    UUID priorityId;
    UUID workOrderStatusId;
    UUID createdBy;
    Instant createdAt;
}
```

### `WorkOrderMaterial`
```java
public class WorkOrderMaterial {
    UUID id;
    UUID workOrderId;
    UUID materialProductId;
    BigDecimal requiredQuantity;
    BigDecimal reservedQuantity;
    BigDecimal consumedQuantity;
}
```

---

## 2. DTOs & Request Objects

### `CreateWorkOrderRequest`
```java
public class CreateWorkOrderRequest {
    @NotBlank(message = "Work Order code is required")
    String code;

    @NotNull(message = "Finished product ID is required")
    UUID finishedProductId;

    @NotNull(message = "Planned quantity is required")
    @DecimalMin(value = "0.0001", message = "Planned quantity must be greater than 0")
    BigDecimal plannedQuantity;

    @NotNull(message = "Planned start date is required")
    Instant plannedStartDate;

    @NotNull(message = "Planned end date is required")
    Instant plannedEndDate;

    UUID priorityId;
}
```

### `WorkOrderDto`
```java
public class WorkOrderDto {
    UUID id;
    String code;
    UUID finishedProductId;
    UUID bomId;
    BigDecimal plannedQuantity;
    Instant plannedStartDate;
    Instant plannedEndDate;
    UUID priorityId;
    UUID workOrderStatusId;
    UUID createdBy;
    Instant createdAt;
    List<WorkOrderMaterialDto> materials;
}
```

---

## 3. Database Mapping (jOOQ Records)

| Table | Record Class | Domain Entity |
|-------|--------------|---------------|
| `public.work_orders` | `WorkOrdersRecord` | `WorkOrder` |
| `public.work_order_materials` | `WorkOrderMaterialsRecord` | `WorkOrderMaterial` |
