# Data Model: Get Work Order Detail (`GET /api/v1/work-orders/{id}`)

## Entities & DTOs

### 1. `WorkOrderDto` (Response DTO)
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
    List<WorkOrderEventDto> events;
}
```

### 2. `WorkOrderMaterialDto` (Child DTO)
```java
public class WorkOrderMaterialDto {
    UUID id;
    UUID workOrderId;
    UUID materialProductId;
    BigDecimal requiredQuantity;
    BigDecimal reservedQuantity;
    BigDecimal consumedQuantity;
}
```

### 3. `WorkOrderEventDto` (Child DTO)
```java
public class WorkOrderEventDto {
    UUID id;
    UUID workOrderId;
    UUID eventTypeId;
    UUID operatorId; // Performer
    Instant eventTimestamp; // Event Time
    String note;
}
```

## Entity Relationships

```mermaid
erDiagram
    WORK_ORDERS ||--o{ WORK_ORDER_MATERIALS : "has material requirements"
    WORK_ORDERS ||--o{ WORK_ORDER_EVENTS : "records history events"
    WORK_ORDERS {
        uuid id PK
        string code
        uuid finished_product_id FK
        uuid bom_id FK
        decimal planned_quantity
        timestamp planned_start_date
        timestamp planned_end_date
        uuid priority_id FK
        uuid work_order_status_id FK
        uuid created_by FK
        timestamp created_at
    }
    WORK_ORDER_MATERIALS {
        uuid id PK
        uuid work_order_id FK
        uuid material_product_id FK
        decimal required_quantity
        decimal reserved_quantity
        decimal consumed_quantity
    }
    WORK_ORDER_EVENTS {
        uuid id PK
        uuid work_order_id FK
        uuid event_type_id FK
        uuid operator_id FK
        timestamp event_timestamp
        string note
    }
```
