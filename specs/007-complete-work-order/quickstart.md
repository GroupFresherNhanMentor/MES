# Quickstart: Validate Complete Work Order

## Prerequisites

- Docker is running and PostgreSQL services are available.
- Backend dependencies and generated jOOQ classes are current after applying migrations.
- Seed data includes Work Order statuses, the active `IN_PROGRESS -> COMPLETED` transition, `COMPLETE` event type, stock statuses, movement types, QC pending status, and the production lot type.

## Run the Backend

From the repository root, start required services. Then run the application from `be/`:

```powershell
docker-compose up -d
```

```powershell
cd be
.\mvnw.cmd spring-boot:run
```

## Run Focused Tests

From `be/`:

```powershell
.\mvnw.cmd test -Dtest=WorkOrderServiceCompleteTest,WorkOrderCompletionIntegrationTest,WorkOrderControllerTest
```

Run the full backend suite before merge:

```powershell
.\mvnw.cmd test
```

## Validate the Happy Path

1. Prepare an `IN_PROGRESS` Work Order with an active production run, a running machine, and material reservations across at least two raw-material lots.
2. Authenticate as an `OPERATOR` and submit the request in [contracts/complete-work-order.md](./contracts/complete-work-order.md).
3. Confirm the response reports the Work Order as completed.
4. Confirm the results described in [data-model.md](./data-model.md):
   - production run is closed and machine is available;
   - good and defective output have separate lots, quality-isolation balances, and pending inspections;
   - material consumption, material scrap, and released reservations reconcile to each original reservation;
   - completion event and `COMPLETE_PRODUCTION` audit entry exist.

## Validate Failure and Concurrency Cases

1. Repeat with a Planner or Administrator token: expect `403` and no data changes.
2. Submit totals that do not equal actual quantity: expect `400` and no data changes.
3. Submit two simultaneous Operator requests for the same Work Order: expect exactly one success, one lifecycle rejection, and one complete set of completion records.
