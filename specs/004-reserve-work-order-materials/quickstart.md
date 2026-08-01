# Quickstart: Reserve Work Order Materials

## Prerequisites

- Java 25 and Maven Wrapper available.
- Docker services running with PostgreSQL:

```bash
docker-compose up -d
```

- A valid JWT for a user with the `PLANNER` role.
- One or more `ACTIVE` warehouses with active locations.
- A finished product with an active BOM and an existing Work Order in `PLANNED` status.
- `work_order_materials` populated for the Work Order.
- `AVAILABLE` stock balances for every required material across active warehouses.

## Start the Backend

```bash
cd be
./mvnw spring-boot:run
```

The API is expected to be available at `http://localhost:8080`.

## Successful Reservation

Replace the UUID values with seeded data and use a valid Planner token:

```bash
curl -X POST "http://localhost:8080/api/work-orders/{workOrderId}/reserve-materials" \
  -H "Authorization: Bearer ${PLANNER_TOKEN}"
```

Expected result:

- HTTP `200 OK`.
- Response data contains the Work Order ID and `READY_TO_PRODUCE`.
- The Work Order status is `READY_TO_PRODUCE`.
- `AVAILABLE` balances decrease and `RESERVED` balances increase in the active warehouses and locations selected by FIFO.
- One `RESERVE` movement exists for each selected lot allocation.
- A `RESERVE_MATERIAL` audit record exists for the status transition.

## Insufficient Stock

Use a Work Order whose required quantity exceeds the available quantity across all active warehouses:

```bash
curl -i -X POST "http://localhost:8080/api/work-orders/{workOrderId}/reserve-materials" \
  -H "Authorization: Bearer ${PLANNER_TOKEN}"
```

Expected result:

- HTTP `400 Bad Request`.
- Error code `INSUFFICIENT_STOCK`.
- Details identify each material and shortage quantity.
- No `AVAILABLE` or `RESERVED` balance changes.
- No `RESERVE` movement is created.
- The Work Order status is `MATERIAL_SHORTAGE` and the transition is audited.

## Active Warehouse Scope

Seed material across two active warehouses and verify the reservation succeeds when their combined quantity is sufficient. Seed adequate material only in an inactive warehouse and verify it is excluded, returning `INSUFFICIENT_STOCK`.

## Machine Assignment

This endpoint does not accept or validate a machine. Validate the selected machine and its availability when starting production.

## FIFO Verification

Create two eligible lots for the same material with different creation times and enough combined stock only when both lots are used. Verify that the older lot is consumed first and each allocation has a corresponding `RESERVE` movement.

## Concurrency Verification

Run the backend integration test suite after implementation:

```bash
cd be
./mvnw -Dtest=WorkOrderIntegrationTest test
```

The concurrency scenario must issue 20 simultaneous reservations for 1 unit against 10 available units and verify exactly 10 successes, 10 `INSUFFICIENT_STOCK` failures, zero negative stock, and no duplicate movements.

## Contract Reference

See [reserve-work-order-materials-api.json](contracts/reserve-work-order-materials-api.json) and [data-model.md](data-model.md) for the complete request, response, state, and persistence rules.
