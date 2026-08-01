package fpt.qn.mes.workorder.integration;

import static fpt.qn.mes.jooq.Tables.AUDIT_LOGS;
import static fpt.qn.mes.jooq.Tables.BOM_STATUSES;
import static fpt.qn.mes.jooq.Tables.BOMS;
import static fpt.qn.mes.jooq.Tables.LOCATION_STATUSES;
import static fpt.qn.mes.jooq.Tables.PRODUCTS;
import static fpt.qn.mes.jooq.Tables.PRODUCT_STATUSES;
import static fpt.qn.mes.jooq.Tables.PRODUCT_TYPES;
import static fpt.qn.mes.jooq.Tables.ROLES;
import static fpt.qn.mes.jooq.Tables.STOCK_BALANCES;
import static fpt.qn.mes.jooq.Tables.STOCK_LOTS;
import static fpt.qn.mes.jooq.Tables.STOCK_MOVEMENTS;
import static fpt.qn.mes.jooq.Tables.STOCK_STATUSES;
import static fpt.qn.mes.jooq.Tables.UNITS_OF_MEASURE;
import static fpt.qn.mes.jooq.Tables.USER_ROLES;
import static fpt.qn.mes.jooq.Tables.USERS;
import static fpt.qn.mes.jooq.Tables.WAREHOUSE_LOCATIONS;
import static fpt.qn.mes.jooq.Tables.WAREHOUSE_STATUSES;
import static fpt.qn.mes.jooq.Tables.WAREHOUSES;
import static fpt.qn.mes.jooq.Tables.WORK_ORDER_MATERIALS;
import static fpt.qn.mes.jooq.Tables.WORK_ORDER_STATUSES;
import static fpt.qn.mes.jooq.Tables.WORK_ORDERS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.jooq.DSLContext;
import org.jooq.JSONB;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

import fpt.qn.mes.AbstractIntegrationTest;
import lombok.AllArgsConstructor;
import lombok.Getter;

class WorkOrderIntegrationTest extends AbstractIntegrationTest {

    @LocalServerPort
    int port;

    @Autowired
    DSLContext dsl;

    RestTemplate restTemplate;

    @BeforeEach
    void setUp() {
        restTemplate = new RestTemplate();
        seedAdminUser();
    }

    @Test
    void reserveMaterials_returns401WithoutAuthentication() {
        assertThatThrownBy(() -> restTemplate.exchange(url(UUID.randomUUID()), HttpMethod.POST,
                new HttpEntity<>(jsonHeaders()), String.class))
                .isInstanceOf(HttpStatusCodeException.class)
                .satisfies(error -> assertThat(((HttpStatusCodeException) error).getStatusCode())
                        .isEqualTo(HttpStatus.UNAUTHORIZED));
    }

    @Test
    void reserveMaterials_returns403ForNonPlanner() {
        HttpHeaders headers = jsonHeaders();
        headers.setBearerAuth(generateAdminToken());

        assertThatThrownBy(() -> restTemplate.exchange(url(UUID.randomUUID()), HttpMethod.POST,
                new HttpEntity<>(headers), String.class))
                .isInstanceOf(HttpStatusCodeException.class)
                .satisfies(error -> assertThat(((HttpStatusCodeException) error).getStatusCode())
                        .isEqualTo(HttpStatus.FORBIDDEN));
    }

    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    void reserveMaterials_reservesAcrossActiveWarehousesWithFifoMovementAndAudit() {
        OffsetDateTime older = OffsetDateTime.now(ZoneOffset.UTC).minusDays(1);
        OffsetDateTime newer = OffsetDateTime.now(ZoneOffset.UTC);
        ReservationFixture fixture = seedFixture(1, BigDecimal.valueOf(5), List.of(
                new StockSeed(true, BigDecimal.valueOf(2), older),
                new StockSeed(true, BigDecimal.valueOf(3), newer)));

        ResponseEntity<String> response = reserve(fixture.getWorkOrderIds().getFirst(), fixture.getUsername());

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).contains("READY_TO_PRODUCE");
        assertThat(workOrderStatus(fixture.getWorkOrderIds().getFirst())).isEqualTo("READY_TO_PRODUCE");
        assertThat(availableQuantity(fixture.getMaterialProductId())).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(reservedQuantity(fixture.getMaterialProductId())).isEqualByComparingTo(BigDecimal.valueOf(5));
        assertThat(workOrderMaterialReservedQuantity(fixture.getWorkOrderIds().getFirst(), fixture.getMaterialProductId()))
                .isEqualByComparingTo(BigDecimal.valueOf(5));
        assertThat(dsl.select(STOCK_MOVEMENTS.QUANTITY).from(STOCK_MOVEMENTS)
                .where(STOCK_MOVEMENTS.WORK_ORDER_ID.eq(fixture.getWorkOrderIds().getFirst()))
                .and(STOCK_MOVEMENTS.LOT_ID.eq(fixture.getLotIds().getFirst()))
                .fetchOne(STOCK_MOVEMENTS.QUANTITY)).isEqualByComparingTo(BigDecimal.valueOf(2));
        assertThat(dsl.select(STOCK_MOVEMENTS.QUANTITY).from(STOCK_MOVEMENTS)
                .where(STOCK_MOVEMENTS.WORK_ORDER_ID.eq(fixture.getWorkOrderIds().getFirst()))
                .and(STOCK_MOVEMENTS.LOT_ID.eq(fixture.getLotIds().get(1)))
                .fetchOne(STOCK_MOVEMENTS.QUANTITY)).isEqualByComparingTo(BigDecimal.valueOf(3));
        assertThat(dsl.selectCount().from(AUDIT_LOGS)
                .where(AUDIT_LOGS.ENTITY_ID.eq(fixture.getWorkOrderIds().getFirst()))
                .and(AUDIT_LOGS.ACTOR_ID.eq(fixture.getUserId()))
                .and(AUDIT_LOGS.ACTION.eq("RESERVE_MATERIAL"))
                .and(AUDIT_LOGS.OLD_VALUE.eq(JSONB.valueOf("\"PLANNED\"")))
                .and(AUDIT_LOGS.NEW_VALUE.eq(JSONB.valueOf("\"READY_TO_PRODUCE\"")))
                .fetchOne(0, Integer.class)).isEqualTo(1);
    }

    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    void reserveMaterials_marksShortageWithoutPartialReservationAndCanRetry() {
        ReservationFixture fixture = seedFixture(1, BigDecimal.valueOf(5), List.of(
                new StockSeed(true, BigDecimal.valueOf(5), OffsetDateTime.now(ZoneOffset.UTC))));
        UUID insufficientMaterialId = addMaterial(fixture, BigDecimal.valueOf(3), BigDecimal.ONE);
        UUID workOrderId = fixture.getWorkOrderIds().getFirst();

        HttpStatusCodeException shortage = org.junit.jupiter.api.Assertions.assertThrows(
                HttpStatusCodeException.class, () -> reserve(workOrderId, fixture.getUsername()));

        assertThat(shortage.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(shortage.getResponseBodyAsString()).contains("INSUFFICIENT_STOCK", insufficientMaterialId.toString());
        assertThat(workOrderStatus(workOrderId)).isEqualTo("MATERIAL_SHORTAGE");
        assertThat(availableQuantity(fixture.getMaterialProductId())).isEqualByComparingTo(BigDecimal.valueOf(5));
        assertThat(availableQuantity(insufficientMaterialId)).isEqualByComparingTo(BigDecimal.ONE);
        assertThat(reservedQuantity(fixture.getMaterialProductId())).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(reservedQuantity(insufficientMaterialId)).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(dsl.fetchCount(STOCK_MOVEMENTS, STOCK_MOVEMENTS.WORK_ORDER_ID.eq(workOrderId))).isZero();

        addStock(fixture, insufficientMaterialId, BigDecimal.valueOf(2), OffsetDateTime.now(ZoneOffset.UTC).plusSeconds(1));
        ResponseEntity<String> retry = reserve(workOrderId, fixture.getUsername());

        assertThat(retry.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(workOrderStatus(workOrderId)).isEqualTo("READY_TO_PRODUCE");
        assertThat(dsl.fetchCount(STOCK_MOVEMENTS, STOCK_MOVEMENTS.WORK_ORDER_ID.eq(workOrderId))).isEqualTo(3);
    }

    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    void reserveMaterials_excludesStockInInactiveWarehouses() {
        ReservationFixture fixture = seedFixture(1, BigDecimal.valueOf(5), List.of(
                new StockSeed(true, BigDecimal.ONE, OffsetDateTime.now(ZoneOffset.UTC).minusDays(1)),
                new StockSeed(false, BigDecimal.valueOf(4), OffsetDateTime.now(ZoneOffset.UTC))));
        UUID workOrderId = fixture.getWorkOrderIds().getFirst();

        HttpStatusCodeException shortage = org.junit.jupiter.api.Assertions.assertThrows(
                HttpStatusCodeException.class, () -> reserve(workOrderId, fixture.getUsername()));

        assertThat(shortage.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(shortage.getResponseBodyAsString()).contains("INSUFFICIENT_STOCK");
        assertThat(workOrderStatus(workOrderId)).isEqualTo("MATERIAL_SHORTAGE");
        assertThat(dsl.select(STOCK_BALANCES.QUANTITY).from(STOCK_BALANCES)
                .where(STOCK_BALANCES.ID.eq(fixture.getBalanceIds().get(1)))
                .fetchOne(STOCK_BALANCES.QUANTITY)).isEqualByComparingTo(BigDecimal.valueOf(4));
    }

    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    void reserveMaterials_rejectsDuplicateReservationWithInvalidInputWithoutNewMovements() {
        ReservationFixture fixture = seedFixture(1, BigDecimal.ONE, List.of(
                new StockSeed(true, BigDecimal.ONE, OffsetDateTime.now(ZoneOffset.UTC))));
        UUID workOrderId = fixture.getWorkOrderIds().getFirst();
        reserve(workOrderId, fixture.getUsername());

        HttpStatusCodeException duplicate = org.junit.jupiter.api.Assertions.assertThrows(
                HttpStatusCodeException.class, () -> reserve(workOrderId, fixture.getUsername()));

        assertThat(duplicate.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(duplicate.getResponseBodyAsString()).contains("INVALID_INPUT");
        assertThat(dsl.fetchCount(STOCK_MOVEMENTS, STOCK_MOVEMENTS.WORK_ORDER_ID.eq(workOrderId))).isEqualTo(1);
        assertThat(workOrderStatus(workOrderId)).isEqualTo("READY_TO_PRODUCE");
    }

    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    void reserveMaterials_preservesStockIntegrityUnderConcurrentRequests() throws InterruptedException {
        ReservationFixture fixture = seedFixture(20, BigDecimal.ONE, List.of(
                new StockSeed(true, BigDecimal.TEN, OffsetDateTime.now(ZoneOffset.UTC))));
        CountDownLatch start = new CountDownLatch(1);
        CountDownLatch done = new CountDownLatch(fixture.getWorkOrderIds().size());
        List<Integer> statusCodes = new CopyOnWriteArrayList<>();
        List<String> errorBodies = new CopyOnWriteArrayList<>();
        ExecutorService pool = Executors.newFixedThreadPool(fixture.getWorkOrderIds().size());

        for (UUID workOrderId : fixture.getWorkOrderIds()) {
            pool.submit(() -> {
                try {
                    start.await();
                    statusCodes.add(reserve(workOrderId, fixture.getUsername()).getStatusCode().value());
                } catch (HttpStatusCodeException exception) {
                    statusCodes.add(exception.getStatusCode().value());
                    errorBodies.add(exception.getResponseBodyAsString());
                } catch (InterruptedException exception) {
                    Thread.currentThread().interrupt();
                    statusCodes.add(HttpStatus.INTERNAL_SERVER_ERROR.value());
                } finally {
                    done.countDown();
                }
            });
        }

        start.countDown();
        assertThat(done.await(30, TimeUnit.SECONDS)).isTrue();
        pool.shutdown();
        assertThat(pool.awaitTermination(5, TimeUnit.SECONDS)).isTrue();

        assertThat(statusCodes.stream().filter(status -> status == HttpStatus.OK.value()).count()).isEqualTo(10);
        assertThat(statusCodes.stream().filter(status -> status == HttpStatus.BAD_REQUEST.value()).count()).isEqualTo(10);
        assertThat(errorBodies).allSatisfy(body -> assertThat(body).contains("INSUFFICIENT_STOCK"));
        assertThat(availableQuantity(fixture.getMaterialProductId())).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(reservedQuantity(fixture.getMaterialProductId())).isEqualByComparingTo(BigDecimal.TEN);
        assertThat(dsl.fetchCount(STOCK_MOVEMENTS, STOCK_MOVEMENTS.WORK_ORDER_ID.in(fixture.getWorkOrderIds())))
                .isEqualTo(10);
        assertThat(dsl.select(org.jooq.impl.DSL.countDistinct(STOCK_MOVEMENTS.WORK_ORDER_ID)).from(STOCK_MOVEMENTS)
                .where(STOCK_MOVEMENTS.WORK_ORDER_ID.in(fixture.getWorkOrderIds()))
                .fetchOne(0, Integer.class)).isEqualTo(10);
        assertThat(dsl.selectCount().from(WORK_ORDERS)
                .join(WORK_ORDER_STATUSES).on(WORK_ORDER_STATUSES.ID.eq(WORK_ORDERS.WORK_ORDER_STATUS_ID))
                .where(WORK_ORDERS.ID.in(fixture.getWorkOrderIds()))
                .and(WORK_ORDER_STATUSES.NAME.eq("READY_TO_PRODUCE"))
                .fetchOne(0, Integer.class)).isEqualTo(10);
        assertThat(dsl.selectCount().from(WORK_ORDERS)
                .join(WORK_ORDER_STATUSES).on(WORK_ORDER_STATUSES.ID.eq(WORK_ORDERS.WORK_ORDER_STATUS_ID))
                .where(WORK_ORDERS.ID.in(fixture.getWorkOrderIds()))
                .and(WORK_ORDER_STATUSES.NAME.eq("MATERIAL_SHORTAGE"))
                .fetchOne(0, Integer.class)).isEqualTo(10);
    }

    private ReservationFixture seedFixture(int workOrderCount, BigDecimal requiredQuantity, List<StockSeed> stockSeeds) {
        UUID userId = UUID.randomUUID();
        String username = "planner_" + userId;
        UUID materialProductId = UUID.randomUUID();
        UUID finishedProductId = UUID.randomUUID();
        UUID bomId = UUID.randomUUID();
        UUID plannerRoleId = referenceId(ROLES.ID, ROLES, ROLES.NAME, "PLANNER");
        UUID rawTypeId = referenceId(PRODUCT_TYPES.ID, PRODUCT_TYPES, PRODUCT_TYPES.NAME, "RAW_MATERIAL");
        UUID finishedTypeId = referenceId(PRODUCT_TYPES.ID, PRODUCT_TYPES, PRODUCT_TYPES.NAME, "FINISHED_GOOD");
        UUID unitId = referenceId(UNITS_OF_MEASURE.ID, UNITS_OF_MEASURE, UNITS_OF_MEASURE.NAME, "PCS");
        UUID productStatusId = referenceId(PRODUCT_STATUSES.ID, PRODUCT_STATUSES, PRODUCT_STATUSES.NAME, "ACTIVE");
        UUID activeWarehouseStatusId = referenceId(WAREHOUSE_STATUSES.ID, WAREHOUSE_STATUSES,
                WAREHOUSE_STATUSES.NAME, "ACTIVE");
        UUID inactiveWarehouseStatusId = referenceId(WAREHOUSE_STATUSES.ID, WAREHOUSE_STATUSES,
                WAREHOUSE_STATUSES.NAME, "INACTIVE");
        UUID locationStatusId = referenceId(LOCATION_STATUSES.ID, LOCATION_STATUSES, LOCATION_STATUSES.NAME, "ACTIVE");
        UUID bomStatusId = referenceId(BOM_STATUSES.ID, BOM_STATUSES, BOM_STATUSES.NAME, "ACTIVE");
        UUID plannedStatusId = referenceId(WORK_ORDER_STATUSES.ID, WORK_ORDER_STATUSES,
                WORK_ORDER_STATUSES.NAME, "PLANNED");
        UUID availableStockStatusId = referenceId(STOCK_STATUSES.ID, STOCK_STATUSES, STOCK_STATUSES.NAME, "AVAILABLE");

        dsl.insertInto(USERS).columns(USERS.ID, USERS.USERNAME, USERS.PASSWORD_HASH, USERS.ACTIVE)
                .values(userId, username, "hash", true).execute();
        dsl.insertInto(USER_ROLES).columns(USER_ROLES.USER_ID, USER_ROLES.ROLE_ID)
                .values(userId, plannerRoleId).execute();
        insertProduct(materialProductId, "MAT_" + materialProductId, rawTypeId, unitId, productStatusId);
        insertProduct(finishedProductId, "FG_" + finishedProductId, finishedTypeId, unitId, productStatusId);
        dsl.insertInto(BOMS).columns(BOMS.ID, BOMS.FINISHED_PRODUCT_ID, BOMS.VERSION, BOMS.BOM_STATUS_ID, BOMS.CREATED_BY)
                .values(bomId, finishedProductId, 1, bomStatusId, userId).execute();

        List<UUID> lotIds = new ArrayList<>();
        List<UUID> balanceIds = new ArrayList<>();
        List<UUID> warehouseIds = new ArrayList<>();
        List<UUID> locationIds = new ArrayList<>();
        for (StockSeed stockSeed : stockSeeds) {
            UUID warehouseId = UUID.randomUUID();
            UUID locationId = UUID.randomUUID();
            UUID lotId = UUID.randomUUID();
            UUID balanceId = UUID.randomUUID();
            dsl.insertInto(WAREHOUSES).columns(WAREHOUSES.ID, WAREHOUSES.CODE, WAREHOUSES.NAME,
                            WAREHOUSES.WAREHOUSE_STATUS_ID)
                    .values(warehouseId, "WH_" + warehouseId, "Warehouse", stockSeed.isActive()
                            ? activeWarehouseStatusId : inactiveWarehouseStatusId)
                    .execute();
            dsl.insertInto(WAREHOUSE_LOCATIONS).columns(WAREHOUSE_LOCATIONS.ID, WAREHOUSE_LOCATIONS.WAREHOUSE_ID,
                            WAREHOUSE_LOCATIONS.CODE, WAREHOUSE_LOCATIONS.NAME, WAREHOUSE_LOCATIONS.LOCATION_STATUS_ID)
                    .values(locationId, warehouseId, "LOC_" + locationId, "Location", locationStatusId)
                    .execute();
            dsl.insertInto(STOCK_LOTS).columns(STOCK_LOTS.ID, STOCK_LOTS.LOT_NUMBER, STOCK_LOTS.PRODUCT_ID,
                            STOCK_LOTS.CREATED_AT)
                    .values(lotId, "LOT_" + lotId, materialProductId, stockSeed.getCreatedAt()).execute();
            dsl.insertInto(STOCK_BALANCES).columns(STOCK_BALANCES.ID, STOCK_BALANCES.WAREHOUSE_ID,
                            STOCK_BALANCES.LOCATION_ID, STOCK_BALANCES.PRODUCT_ID, STOCK_BALANCES.LOT_ID,
                            STOCK_BALANCES.STOCK_STATUS_ID, STOCK_BALANCES.QUANTITY)
                    .values(balanceId, warehouseId, locationId, materialProductId, lotId, availableStockStatusId,
                            stockSeed.getQuantity())
                    .execute();
            lotIds.add(lotId);
            balanceIds.add(balanceId);
            warehouseIds.add(warehouseId);
            locationIds.add(locationId);
        }

        List<UUID> workOrderIds = new ArrayList<>();
        for (int index = 0; index < workOrderCount; index++) {
            UUID workOrderId = UUID.randomUUID();
            dsl.insertInto(WORK_ORDERS).columns(WORK_ORDERS.ID, WORK_ORDERS.CODE, WORK_ORDERS.FINISHED_PRODUCT_ID,
                            WORK_ORDERS.BOM_ID, WORK_ORDERS.PLANNED_QUANTITY, WORK_ORDERS.WORK_ORDER_STATUS_ID,
                            WORK_ORDERS.CREATED_BY)
                    .values(workOrderId, "WO_" + workOrderId, finishedProductId, bomId, BigDecimal.ONE,
                            plannedStatusId, userId)
                    .execute();
            dsl.insertInto(WORK_ORDER_MATERIALS).columns(WORK_ORDER_MATERIALS.ID, WORK_ORDER_MATERIALS.WORK_ORDER_ID,
                            WORK_ORDER_MATERIALS.MATERIAL_PRODUCT_ID, WORK_ORDER_MATERIALS.REQUIRED_QUANTITY,
                            WORK_ORDER_MATERIALS.RESERVED_QUANTITY, WORK_ORDER_MATERIALS.CONSUMED_QUANTITY)
                    .values(UUID.randomUUID(), workOrderId, materialProductId, requiredQuantity, BigDecimal.ZERO,
                            BigDecimal.ZERO)
                    .execute();
            workOrderIds.add(workOrderId);
        }

        return new ReservationFixture(userId, username, materialProductId, unitId, productStatusId,
                availableStockStatusId, workOrderIds, lotIds, balanceIds, warehouseIds, locationIds);
    }

    private UUID addMaterial(ReservationFixture fixture, BigDecimal requiredQuantity, BigDecimal availableQuantity) {
        UUID materialProductId = UUID.randomUUID();
        insertProduct(materialProductId, "MAT_" + materialProductId,
                referenceId(PRODUCT_TYPES.ID, PRODUCT_TYPES, PRODUCT_TYPES.NAME, "RAW_MATERIAL"),
                fixture.getUnitId(), fixture.getProductStatusId());
        UUID workOrderId = fixture.getWorkOrderIds().getFirst();
        dsl.insertInto(WORK_ORDER_MATERIALS).columns(WORK_ORDER_MATERIALS.ID, WORK_ORDER_MATERIALS.WORK_ORDER_ID,
                        WORK_ORDER_MATERIALS.MATERIAL_PRODUCT_ID, WORK_ORDER_MATERIALS.REQUIRED_QUANTITY,
                        WORK_ORDER_MATERIALS.RESERVED_QUANTITY, WORK_ORDER_MATERIALS.CONSUMED_QUANTITY)
                .values(UUID.randomUUID(), workOrderId, materialProductId, requiredQuantity, BigDecimal.ZERO,
                        BigDecimal.ZERO)
                .execute();
        addStock(fixture, materialProductId, availableQuantity, OffsetDateTime.now(ZoneOffset.UTC));
        return materialProductId;
    }

    private void addStock(ReservationFixture fixture, UUID productId, BigDecimal quantity, OffsetDateTime createdAt) {
        UUID lotId = UUID.randomUUID();
        dsl.insertInto(STOCK_LOTS).columns(STOCK_LOTS.ID, STOCK_LOTS.LOT_NUMBER, STOCK_LOTS.PRODUCT_ID,
                        STOCK_LOTS.CREATED_AT)
                .values(lotId, "LOT_" + lotId, productId, createdAt).execute();
        dsl.insertInto(STOCK_BALANCES).columns(STOCK_BALANCES.ID, STOCK_BALANCES.WAREHOUSE_ID,
                        STOCK_BALANCES.LOCATION_ID, STOCK_BALANCES.PRODUCT_ID, STOCK_BALANCES.LOT_ID,
                        STOCK_BALANCES.STOCK_STATUS_ID, STOCK_BALANCES.QUANTITY)
                .values(UUID.randomUUID(), fixture.getWarehouseIds().getFirst(), fixture.getLocationIds().getFirst(),
                        productId, lotId, fixture.getAvailableStockStatusId(), quantity)
                .execute();
    }

    private void insertProduct(UUID productId, String code, UUID productTypeId, UUID unitId, UUID productStatusId) {
        dsl.insertInto(PRODUCTS).columns(PRODUCTS.ID, PRODUCTS.CODE, PRODUCTS.NAME, PRODUCTS.PRODUCT_TYPE_ID,
                        PRODUCTS.UNIT_ID, PRODUCTS.PRODUCT_STATUS_ID)
                .values(productId, code, code, productTypeId, unitId, productStatusId)
                .execute();
    }

    private UUID referenceId(org.jooq.Field<UUID> idField, org.jooq.Table<?> table,
            org.jooq.Field<String> nameField, String name) {
        return dsl.select(idField).from(table).where(nameField.eq(name)).fetchOne(idField);
    }

    private ResponseEntity<String> reserve(UUID workOrderId, String username) {
        HttpHeaders headers = jsonHeaders();
        headers.setBearerAuth(generateToken(username, "PLANNER"));
        return restTemplate.exchange(url(workOrderId), HttpMethod.POST, new HttpEntity<>(headers), String.class);
    }

    private String workOrderStatus(UUID workOrderId) {
        return dsl.select(WORK_ORDER_STATUSES.NAME).from(WORK_ORDERS)
                .join(WORK_ORDER_STATUSES).on(WORK_ORDER_STATUSES.ID.eq(WORK_ORDERS.WORK_ORDER_STATUS_ID))
                .where(WORK_ORDERS.ID.eq(workOrderId))
                .fetchOne(WORK_ORDER_STATUSES.NAME);
    }

    private BigDecimal availableQuantity(UUID productId) {
        return quantityByStatus(productId, "AVAILABLE");
    }

    private BigDecimal reservedQuantity(UUID productId) {
        return quantityByStatus(productId, "RESERVED");
    }

    private BigDecimal quantityByStatus(UUID productId, String stockStatus) {
        return dsl.select(org.jooq.impl.DSL.coalesce(org.jooq.impl.DSL.sum(STOCK_BALANCES.QUANTITY), BigDecimal.ZERO))
                .from(STOCK_BALANCES)
                .join(STOCK_STATUSES).on(STOCK_STATUSES.ID.eq(STOCK_BALANCES.STOCK_STATUS_ID))
                .where(STOCK_BALANCES.PRODUCT_ID.eq(productId))
                .and(STOCK_STATUSES.NAME.eq(stockStatus))
                .fetchOne(0, BigDecimal.class);
    }

    private BigDecimal workOrderMaterialReservedQuantity(UUID workOrderId, UUID productId) {
        return dsl.select(WORK_ORDER_MATERIALS.RESERVED_QUANTITY).from(WORK_ORDER_MATERIALS)
                .where(WORK_ORDER_MATERIALS.WORK_ORDER_ID.eq(workOrderId))
                .and(WORK_ORDER_MATERIALS.MATERIAL_PRODUCT_ID.eq(productId))
                .fetchOne(WORK_ORDER_MATERIALS.RESERVED_QUANTITY);
    }

    private String url(UUID workOrderId) {
        return "http://localhost:" + port + "/api/work-orders/" + workOrderId + "/reserve-materials";
    }

    private HttpHeaders jsonHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Content-Type", "application/json");
        return headers;
    }

    @Getter
    @AllArgsConstructor
    private static class StockSeed {
        boolean active;
        BigDecimal quantity;
        OffsetDateTime createdAt;
    }

    @Getter
    @AllArgsConstructor
    private static class ReservationFixture {
        UUID userId;
        String username;
        UUID materialProductId;
        UUID unitId;
        UUID productStatusId;
        UUID availableStockStatusId;
        List<UUID> workOrderIds;
        List<UUID> lotIds;
        List<UUID> balanceIds;
        List<UUID> warehouseIds;
        List<UUID> locationIds;
    }
}
