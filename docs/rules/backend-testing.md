# Backend Testing Rules

## SpecKit Rule — Tests Are Always Required

When generating tasks with `/speckit-tasks`, **every user story must include test tasks**.
Tests are never optional. The order within each story is:

1. Write unit test → confirm it **fails**
2. Write integration test → confirm it **fails**
3. Implement the feature → both tests pass

If a `tasks.md` is generated without test tasks for a user story, it is incomplete.

---

## Test Pyramid

```
           ┌──────────────┐
           │  Integration │  AbstractIntegrationTest — full context + Testcontainers
           │   Tests      │  slow; use for cross-layer flows (HTTP → service → DB)
           ├──────────────┤
           │  Unit Tests  │  JUnit 5 + Mockito — no Spring context
           │              │  fastest; test service/domain logic in isolation
           └──────────────┘
```

---

## Required Test Coverage Checklist

Every feature must cover ALL of the following before it is considered done:

### Functional (happy path)
- [ ] Normal input returns correct response and persists correct data
- [ ] Paginated list returns correct page/size/total
- [ ] Sub-resource operations (e.g., add BOM item) affect parent correctly

### Input validation & error handling
- [ ] Missing required fields → `400` with field-level error details
- [ ] Invalid UUID / wrong type → `400`
- [ ] Non-existent resource → `404` with correct error code
- [ ] Duplicate creation (unique constraint) → `409`
- [ ] Out-of-range values (negative quantity, version ≤ 0) → `400`

### Authorization
- [ ] No token → `401`
- [ ] Wrong role → `403`
- [ ] Correct role → `200/201`

### Business rules
- [ ] Domain invariants enforced (e.g., cannot release a DRAFT work order directly to IN_PROGRESS)
- [ ] Status transition guards (only valid transitions allowed)
- [ ] Cannot delete a resource that is referenced by another

### Concurrency & data integrity (see dedicated section below)
- [ ] Concurrent writes to the same row do not corrupt data
- [ ] Stock/quantity never goes negative under concurrent consumption
- [ ] Duplicate-key scenarios under concurrent inserts are handled gracefully
- [ ] Optimistic locking detects stale updates

---

## Unit Tests — Service Layer

Use `@ExtendWith(MockitoExtension.class)`. No Spring context. Mock the repository interface.

```java
@ExtendWith(MockitoExtension.class)
class BomServiceTest {

    @Mock BomRepository bomRepository;
    @Mock BomDtoMapper mapper;
    @InjectMocks BomService bomService;

    @Test
    void getBomById_throwsNotFound_whenBomDoesNotExist() {
        UUID id = UUID.randomUUID();
        when(bomRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(BomNotFoundException.class, () -> bomService.getBomById(id));
    }

    @Test
    void createBom_returnsMappedDto() {
        CreateBomRequest request = new CreateBomRequest();
        request.setFinishedProductId(UUID.randomUUID());
        request.setVersion(1);
        request.setBomStatusId(UUID.randomUUID());

        Bom savedBom = Bom.create(request.getFinishedProductId(), request.getVersion(),
            request.getBomStatusId(), UUID.randomUUID());
        BomDto expectedDto = BomDto.builder().id(savedBom.getId()).build();

        when(bomRepository.save(any(Bom.class))).thenReturn(savedBom);
        when(mapper.toDto(savedBom)).thenReturn(expectedDto);

        BomDto result = bomService.createBom(request, UUID.randomUUID());

        assertThat(result.getId()).isEqualTo(expectedDto.getId());
    }
}
```

Rules:
- `@Mock` on repository/mapper, `@InjectMocks` on service
- Test every branch: happy path + each exception path
- Never use `@SpringBootTest` for service tests — it's 50–100× slower with no benefit

---

## Integration Tests — Full Stack

Extend `AbstractIntegrationTest`. Annotated with `@Transactional @Rollback` so each test
rolls back its writes. Use `generateAdminToken()` / `generateToken(userId, role)` for JWT.

```java
class BomIntegrationTest extends AbstractIntegrationTest {

    @Autowired TestRestTemplate restTemplate;

    @Test
    void createBom_returns201_whenAdmin() {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(generateAdminToken());

        CreateBomRequest request = new CreateBomRequest();
        request.setFinishedProductId(UUID.randomUUID());
        request.setVersion(1);
        request.setBomStatusId(UUID.randomUUID());

        ResponseEntity<ApiResponse> response = restTemplate.exchange(
            "/api/boms", HttpMethod.POST,
            new HttpEntity<>(request, headers), ApiResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    }

    @Test
    void getBoms_returns401_whenUnauthenticated() {
        ResponseEntity<String> response = restTemplate.getForEntity("/api/boms", String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }
}
```

---

## Concurrency & Race Condition Tests (HIGH PRIORITY)

MES data is especially vulnerable to concurrency bugs: stock going negative, a work order
started twice, the same lot reserved by two orders simultaneously. These must be tested explicitly.

### Pattern: concurrent requests via ExecutorService

Use `ExecutorService` + `CountDownLatch` to fire N threads simultaneously and assert
that exactly one wins and the rest fail or produce a consistent result.

```java
@Test
void reserveStock_onlySuceedsOnce_whenQuantityIsOne() throws InterruptedException {
    // seed: 1 unit of stock available
    UUID lotId = seedStockLot(quantity = 1);
    int threads = 10;
    CountDownLatch start = new CountDownLatch(1);
    CountDownLatch done = new CountDownLatch(threads);
    List<Integer> statusCodes = new CopyOnWriteArrayList<>();

    ExecutorService pool = Executors.newFixedThreadPool(threads);
    for (int i = 0; i < threads; i++) {
        pool.submit(() -> {
            try {
                start.await();
                HttpHeaders h = new HttpHeaders();
                h.setBearerAuth(generateToken(UUID.randomUUID(), "OPERATOR"));
                ResponseEntity<String> r = restTemplate.exchange(
                    "/api/stock-lots/" + lotId + "/reserve",
                    HttpMethod.POST, new HttpEntity<>(h), String.class);
                statusCodes.add(r.getStatusCode().value());
            } catch (Exception e) {
                statusCodes.add(500);
            } finally {
                done.countDown();
            }
        });
    }

    start.countDown(); // release all threads at once
    done.await(10, TimeUnit.SECONDS);
    pool.shutdown();

    long successes = statusCodes.stream().filter(s -> s == 200).count();
    assertThat(successes).isEqualTo(1); // exactly one reservation wins
}
```

### Pattern: duplicate insert race

Two threads try to create the same unique resource (e.g., same username, same BOM version
for the same product). Exactly one should succeed with `201`, the other should get `409`.

```java
@Test
void createBom_returns409_whenSameVersionCreatedConcurrently() throws InterruptedException {
    UUID productId = UUID.randomUUID();
    int threads = 2;
    CountDownLatch start = new CountDownLatch(1);
    CountDownLatch done = new CountDownLatch(threads);
    List<Integer> statusCodes = new CopyOnWriteArrayList<>();

    ExecutorService pool = Executors.newFixedThreadPool(threads);
    for (int i = 0; i < threads; i++) {
        pool.submit(() -> {
            try {
                start.await();
                // both try to create BOM v1 for same product
                ResponseEntity<String> r = restTemplate.exchange(
                    "/api/boms", HttpMethod.POST,
                    new HttpEntity<>(bomRequest(productId, version = 1), adminHeaders()),
                    String.class);
                statusCodes.add(r.getStatusCode().value());
            } catch (Exception e) {
                statusCodes.add(500);
            } finally {
                done.countDown();
            }
        });
    }

    start.await();
    done.await(10, TimeUnit.SECONDS);
    pool.shutdown();

    assertThat(statusCodes).containsExactlyInAnyOrder(201, 409);
}
```

### Pattern: quantity never goes negative

After N concurrent consume/reserve operations where total requested > available, the
persisted quantity must never be negative.

```java
@Test
void consumeStock_quantityNeverGoesNegative_underConcurrency() throws InterruptedException {
    // seed: 5 units
    UUID lotId = seedStockLot(quantity = 5);
    int threads = 20; // 20 requests each consuming 1, only 5 should succeed

    CountDownLatch start = new CountDownLatch(1);
    CountDownLatch done = new CountDownLatch(threads);
    ExecutorService pool = Executors.newFixedThreadPool(threads);

    for (int i = 0; i < threads; i++) {
        pool.submit(() -> {
            try {
                start.await();
                restTemplate.exchange("/api/stock-lots/" + lotId + "/consume?qty=1",
                    HttpMethod.POST, new HttpEntity<>(operatorHeaders()), String.class);
            } catch (Exception ignored) {
            } finally {
                done.countDown();
            }
        });
    }

    start.countDown();
    done.await(10, TimeUnit.SECONDS);
    pool.shutdown();

    // verify DB state directly
    Integer remaining = fetchQuantityFromDb(lotId);
    assertThat(remaining).isGreaterThanOrEqualTo(0); // never negative
}
```

### Key MES concurrency scenarios to always test

| Scenario | Risk | Test assertion |
|----------|------|----------------|
| Two work orders reserve the same lot | Double-reservation | Only 1 succeeds, quantity = 0 after |
| Concurrent consume beyond available stock | Quantity goes negative | `quantity >= 0` always |
| Two users create same BOM version | Duplicate BOM | Exactly 1 × `201`, 1 × `409` |
| Work order status transition by two threads | Invalid state (e.g., IN_PROGRESS twice) | Final status is valid, transition count = 1 |
| Two threads create user with same username | Duplicate user | Exactly 1 × `201`, 1 × `409` |
| Concurrent machine assignment to two work orders | Machine double-booked | Only 1 assignment persists |

### Implementation guidance: how to prevent the bug, not just detect it

Concurrency tests tell you when you have a bug. Fix it with one of:

- **`SELECT FOR UPDATE`** in jOOQ — locks the row for the duration of the transaction:
  ```java
  dslCtx.selectFrom(STOCK_LOTS)
      .where(STOCK_LOTS.ID.eq(id))
      .forUpdate()       // ← row-level lock
      .fetchOne();
  ```
- **Optimistic locking** — add a `version` column, increment on update, reject stale writes:
  ```java
  int updated = dslCtx.update(STOCK_LOTS)
      .set(STOCK_LOTS.QUANTITY, newQty)
      .set(STOCK_LOTS.VERSION, currentVersion + 1)
      .where(STOCK_LOTS.ID.eq(id))
      .and(STOCK_LOTS.VERSION.eq(currentVersion)) // fails if someone else updated first
      .execute();
  if (updated == 0) throw new AppException(HttpStatus.CONFLICT, ErrorCode.CONFLICT, "Stale update");
  ```
- **Database unique constraint** — for duplicate-creation races, a `UNIQUE` constraint is the
  last line of defence. The application must handle the resulting `DataIntegrityViolationException`
  and translate it to `409 Conflict`.

---

## Test File Location & Naming

```
src/test/java/fpt/qn/mes/
├── AbstractIntegrationTest.java         # base class — extend for all integration tests
├── {module}/
│   ├── application/service/
│   │   └── {Entity}ServiceTest.java     # unit tests
│   └── integration/
│       └── {Entity}IntegrationTest.java # functional + concurrency tests in one file
```

---

## General Rules

| Rule | Why |
|------|-----|
| Unit tests must not start a Spring context | Speed — Spring Boot adds 2–10 seconds per class |
| Functional-only integration tests may use `@Transactional @Rollback` | Do not place class-level `@Transactional` on an integration class that also contains concurrency methods; use explicit database cleanup there |
| Concurrency tests must NOT use `@Transactional` | Transactions must commit for locks to be visible across threads |
| Never call a real repository in a unit test | Use mocks; real DB belongs in integration tests |
| Test exception paths, not just happy paths | `*NotFoundException`, `400`, `401`, `403` |
| `assertThat` from AssertJ — not `assertEquals` from JUnit | More readable failure messages |
| Use `CopyOnWriteArrayList` to collect results from threads | Thread-safe collection across concurrent test threads |
| Test pagination for every paginated list | Assert page, size, totals, and database ordering |
| Test approved legacy catalogs as bounded compatibility contracts | Only the exceptions listed in `backend-api.md` may return `List<T>`; assert unchanged response shape and deterministic ordering |
| Concurrency tests: use `CountDownLatch` for simultaneous start | Without it, threads run sequentially — the race never happens |
