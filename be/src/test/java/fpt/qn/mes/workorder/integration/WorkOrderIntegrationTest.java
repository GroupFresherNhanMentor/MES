package fpt.qn.mes.workorder.integration;

import static fpt.qn.mes.jooq.Tables.AUDIT_LOGS;
import static fpt.qn.mes.jooq.Tables.BOM_STATUSES;
import static fpt.qn.mes.jooq.Tables.BOMS;
import static fpt.qn.mes.jooq.Tables.MACHINE_STATUSES;
import static fpt.qn.mes.jooq.Tables.MACHINES;
import static fpt.qn.mes.jooq.Tables.PRODUCTS;
import static fpt.qn.mes.jooq.Tables.PRODUCT_STATUSES;
import static fpt.qn.mes.jooq.Tables.PRODUCT_TYPES;
import static fpt.qn.mes.jooq.Tables.PRODUCTION_RUNS;
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
import static fpt.qn.mes.jooq.Tables.WORK_ORDER_EVENTS;
import static fpt.qn.mes.jooq.Tables.WORK_ORDER_MATERIALS;
import static fpt.qn.mes.jooq.Tables.WORK_ORDER_STATUSES;
import static fpt.qn.mes.jooq.Tables.WORK_ORDER_STATUS_TRANSITIONS;
import static fpt.qn.mes.jooq.Tables.WORK_ORDERS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.DefaultApplicationArguments;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.jooq.DSLContext;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

import fpt.qn.mes.AbstractIntegrationTest;
import fpt.qn.mes.auth.application.security.AppUserPrincipal;
import fpt.qn.mes.workorder.application.dto.request.ReserveWorkOrderMaterialsRequest;
import fpt.qn.mes.workorder.application.dto.request.StartWorkOrderRequest;
import fpt.qn.mes.workorder.application.exception.WorkOrderExceptions.MachineNotAvailableException;
import fpt.qn.mes.workorder.application.service.WorkOrderService;
import fpt.qn.mes.workorder.infrastructure.seed.WorkOrderDataSeeder;

class WorkOrderIntegrationTest extends AbstractIntegrationTest {

    @LocalServerPort
    int port;

    RestTemplate restTemplate;

    @Autowired
    DSLContext dsl;

    @Autowired
    WorkOrderService workOrderService;

    @Autowired
    WorkOrderDataSeeder workOrderDataSeeder;

    @BeforeEach
    void setUp() {
        restTemplate = new RestTemplate();
        seedAdminUser();
    }

    private String url() {
        return "http://localhost:" + port + "/api/work-orders/"
                + UUID.randomUUID() + "/reserve-materials";
    }

    @Test
    void reserveMaterials_returns401WithoutAuthentication() {
        var request = new HttpEntity<>("{\"machineId\":\"" + UUID.randomUUID() + "\"}", jsonHeaders());

        assertThatThrownBy(() -> restTemplate.exchange(url(), HttpMethod.POST, request, String.class))
                .isInstanceOf(HttpStatusCodeException.class)
                .satisfies(error -> assertThat(((HttpStatusCodeException) error).getStatusCode())
                        .isEqualTo(HttpStatus.UNAUTHORIZED));
    }

    @Test
    void reserveMaterials_returns403ForNonPlanner() {
        HttpHeaders headers = jsonHeaders();
        headers.setBearerAuth(generateAdminToken());

        assertThatThrownBy(() -> restTemplate.exchange(url(), HttpMethod.POST,
                new HttpEntity<>("{\"machineId\":\"" + UUID.randomUUID() + "\"}", headers), String.class))
                .isInstanceOf(HttpStatusCodeException.class)
                .satisfies(error -> assertThat(((HttpStatusCodeException) error).getStatusCode())
                        .isEqualTo(HttpStatus.FORBIDDEN));
    }

    @Test
    void lifecycleSeed_containsReleaseAndDraftCancellationTransitions() {
        UUID draftStatusId = statusId("DRAFT");
        UUID plannedStatusId = statusId("PLANNED");
        UUID readyStatusId = statusId("READY_TO_PRODUCE");
        UUID cancelledStatusId = statusId("CANCELLED");

        assertThat(hasActiveTransition(readyStatusId, plannedStatusId)).isTrue();
        assertThat(hasActiveTransition(draftStatusId, cancelledStatusId)).isTrue();
    }

    @Test
    void lifecycleSeed_isIdempotent() {
        int transitionsBefore = dsl.fetchCount(WORK_ORDER_STATUS_TRANSITIONS);

        workOrderDataSeeder.run(new DefaultApplicationArguments(new String[0]));
        workOrderDataSeeder.run(new DefaultApplicationArguments(new String[0]));

        assertThat(dsl.fetchCount(WORK_ORDER_STATUS_TRANSITIONS)).isEqualTo(transitionsBefore);
    }

    @Test
    void startWorkOrder_allowsOnlyOneAssignment_whenMachineRequestsAreConcurrent() throws InterruptedException {
        UUID operatorId = UUID.randomUUID();
        UUID finishedProductId = UUID.randomUUID();
        UUID bomId = UUID.randomUUID();
        UUID machineId = UUID.randomUUID();
        UUID firstWorkOrderId = UUID.randomUUID();
        UUID secondWorkOrderId = UUID.randomUUID();
        UUID finishedTypeId = dsl.select(PRODUCT_TYPES.ID).from(PRODUCT_TYPES)
                .where(PRODUCT_TYPES.NAME.eq("FINISHED_GOOD")).fetchOne(PRODUCT_TYPES.ID);
        UUID unitId = dsl.select(UNITS_OF_MEASURE.ID).from(UNITS_OF_MEASURE)
                .where(UNITS_OF_MEASURE.NAME.eq("PCS")).fetchOne(UNITS_OF_MEASURE.ID);
        UUID productStatusId = dsl.select(PRODUCT_STATUSES.ID).from(PRODUCT_STATUSES)
                .where(PRODUCT_STATUSES.NAME.eq("ACTIVE")).fetchOne(PRODUCT_STATUSES.ID);
        UUID bomStatusId = dsl.select(BOM_STATUSES.ID).from(BOM_STATUSES)
                .where(BOM_STATUSES.NAME.eq("ACTIVE")).fetchOne(BOM_STATUSES.ID);
        UUID availableMachineStatusId = dsl.select(MACHINE_STATUSES.ID).from(MACHINE_STATUSES)
                .where(MACHINE_STATUSES.NAME.eq("AVAILABLE")).fetchOne(MACHINE_STATUSES.ID);
        UUID readyStatusId = statusId("READY_TO_PRODUCE");
        UUID inProgressStatusId = statusId("IN_PROGRESS");

        dsl.insertInto(USERS).columns(USERS.ID, USERS.USERNAME, USERS.PASSWORD_HASH, USERS.ACTIVE)
                .values(operatorId, "operator_" + operatorId, "hash", true).execute();
        dsl.insertInto(PRODUCTS).columns(PRODUCTS.ID, PRODUCTS.CODE, PRODUCTS.NAME, PRODUCTS.PRODUCT_TYPE_ID,
                        PRODUCTS.UNIT_ID, PRODUCTS.PRODUCT_STATUS_ID)
                .values(finishedProductId, "FG_" + finishedProductId, "Concurrent finished product", finishedTypeId,
                        unitId, productStatusId)
                .execute();
        dsl.insertInto(BOMS).columns(BOMS.ID, BOMS.FINISHED_PRODUCT_ID, BOMS.VERSION, BOMS.BOM_STATUS_ID,
                        BOMS.CREATED_BY)
                .values(bomId, finishedProductId, 1, bomStatusId, operatorId)
                .execute();
        dsl.insertInto(MACHINES).columns(MACHINES.ID, MACHINES.CODE, MACHINES.NAME, MACHINES.MACHINE_STATUS_ID)
                .values(machineId, "M_" + machineId, "Concurrent machine", availableMachineStatusId)
                .execute();
        dsl.insertInto(WORK_ORDERS).columns(WORK_ORDERS.ID, WORK_ORDERS.CODE, WORK_ORDERS.FINISHED_PRODUCT_ID,
                        WORK_ORDERS.BOM_ID, WORK_ORDERS.PLANNED_QUANTITY, WORK_ORDERS.WORK_ORDER_STATUS_ID,
                        WORK_ORDERS.CREATED_BY)
                .values(firstWorkOrderId, "WO_" + firstWorkOrderId, finishedProductId, bomId, BigDecimal.ONE,
                        readyStatusId, operatorId)
                .values(secondWorkOrderId, "WO_" + secondWorkOrderId, finishedProductId, bomId, BigDecimal.ONE,
                        readyStatusId, operatorId)
                .execute();

        CountDownLatch start = new CountDownLatch(1);
        CountDownLatch done = new CountDownLatch(2);
        List<UUID> successfulWorkOrders = new CopyOnWriteArrayList<>();
        List<Throwable> failures = new CopyOnWriteArrayList<>();
        var executor = Executors.newFixedThreadPool(2);
        var operatorPrincipal = AppUserPrincipal.builder()
                .id(operatorId)
                .username("operator_" + operatorId)
                .roles(List.of("OPERATOR"))
                .enabled(true)
                .build();
        for (UUID workOrderId : List.of(firstWorkOrderId, secondWorkOrderId)) {
            executor.submit(() -> {
                try {
                    start.await();
                    SecurityContextHolder.getContext().setAuthentication(
                            new UsernamePasswordAuthenticationToken(operatorPrincipal, "n/a",
                                    List.of(new SimpleGrantedAuthority("ROLE_OPERATOR"))));
                    workOrderService.startWorkOrder(workOrderId, StartWorkOrderRequest.builder()
                            .machineId(machineId)
                            .operatorId(operatorId)
                            .build());
                    successfulWorkOrders.add(workOrderId);
                } catch (InterruptedException exception) {
                    Thread.currentThread().interrupt();
                    failures.add(exception);
                } catch (RuntimeException exception) {
                    failures.add(exception);
                } finally {
                    SecurityContextHolder.clearContext();
                    done.countDown();
                }
            });
        }

        start.countDown();
        boolean completed = done.await(15, TimeUnit.SECONDS);
        executor.shutdownNow();

        assertThat(completed).isTrue();
        assertThat(successfulWorkOrders).hasSize(1);
        assertThat(failures).hasSize(1);
        assertThat(failures.getFirst()).isInstanceOf(MachineNotAvailableException.class);
        assertThat(dsl.fetchCount(PRODUCTION_RUNS,
                PRODUCTION_RUNS.MACHINE_ID.eq(machineId).and(PRODUCTION_RUNS.END_TIME.isNull()))).isEqualTo(1);
        assertThat(dsl.fetchCount(WORK_ORDER_EVENTS,
                WORK_ORDER_EVENTS.WORK_ORDER_ID.in(firstWorkOrderId, secondWorkOrderId))).isEqualTo(1);
        assertThat(dsl.fetchCount(AUDIT_LOGS,
                AUDIT_LOGS.ENTITY_ID.in(firstWorkOrderId, secondWorkOrderId)
                        .and(AUDIT_LOGS.ACTION.eq("START_PRODUCTION")))).isEqualTo(1);
        assertThat(dsl.fetchCount(WORK_ORDERS,
                WORK_ORDERS.ID.in(firstWorkOrderId, secondWorkOrderId)
                        .and(WORK_ORDERS.WORK_ORDER_STATUS_ID.eq(inProgressStatusId)))).isEqualTo(1);
        assertThat(dsl.fetchCount(WORK_ORDERS,
                WORK_ORDERS.ID.in(firstWorkOrderId, secondWorkOrderId)
                        .and(WORK_ORDERS.WORK_ORDER_STATUS_ID.eq(readyStatusId)))).isEqualTo(1);
        assertThat(dsl.select(MACHINE_STATUSES.NAME)
                .from(MACHINES)
                .join(MACHINE_STATUSES).on(MACHINES.MACHINE_STATUS_ID.eq(MACHINE_STATUSES.ID))
                .where(MACHINES.ID.eq(machineId))
                .fetchOne(MACHINE_STATUSES.NAME)).isEqualTo("RUNNING");
    }

    @Test
    void reserveMaterials_updatesStockWorkOrderMovementAndAudit() {
        UUID userId = UUID.randomUUID();
        UUID workOrderId = UUID.randomUUID();
        UUID materialProductId = UUID.randomUUID();
        UUID finishedProductId = UUID.randomUUID();
        UUID warehouseId = UUID.randomUUID();
        UUID locationId = UUID.randomUUID();
        UUID lotId = UUID.randomUUID();
        UUID machineId = UUID.randomUUID();
        UUID bomId = UUID.randomUUID();
        UUID workOrderMaterialId = UUID.randomUUID();

        UUID plannerRoleId = dsl.select(ROLES.ID).from(ROLES).where(ROLES.NAME.eq("PLANNER"))
                .fetchOne(ROLES.ID);
        UUID rawTypeId = dsl.select(PRODUCT_TYPES.ID).from(PRODUCT_TYPES)
                .where(PRODUCT_TYPES.NAME.eq("RAW_MATERIAL")).fetchOne(PRODUCT_TYPES.ID);
        UUID finishedTypeId = dsl.select(PRODUCT_TYPES.ID).from(PRODUCT_TYPES)
                .where(PRODUCT_TYPES.NAME.eq("FINISHED_GOOD")).fetchOne(PRODUCT_TYPES.ID);
        UUID unitId = dsl.select(UNITS_OF_MEASURE.ID).from(UNITS_OF_MEASURE)
                .where(UNITS_OF_MEASURE.NAME.eq("PCS")).fetchOne(UNITS_OF_MEASURE.ID);
        UUID activeProductStatusId = dsl.select(PRODUCT_STATUSES.ID).from(PRODUCT_STATUSES)
                .where(PRODUCT_STATUSES.NAME.eq("ACTIVE")).fetchOne(PRODUCT_STATUSES.ID);
        UUID activeWarehouseStatusId = dsl.select(WAREHOUSE_STATUSES.ID).from(WAREHOUSE_STATUSES)
                .where(WAREHOUSE_STATUSES.NAME.eq("ACTIVE")).fetchOne(WAREHOUSE_STATUSES.ID);
        UUID activeMachineStatusId = dsl.select(MACHINE_STATUSES.ID).from(MACHINE_STATUSES)
                .where(MACHINE_STATUSES.NAME.eq("AVAILABLE")).fetchOne(MACHINE_STATUSES.ID);
        UUID activeBomStatusId = dsl.select(BOM_STATUSES.ID).from(BOM_STATUSES)
                .where(BOM_STATUSES.NAME.eq("ACTIVE")).fetchOne(BOM_STATUSES.ID);
        UUID plannedStatusId = dsl.select(WORK_ORDER_STATUSES.ID).from(WORK_ORDER_STATUSES)
                .where(WORK_ORDER_STATUSES.NAME.eq("PLANNED")).fetchOne(WORK_ORDER_STATUSES.ID);
        UUID availableStockStatusId = dsl.select(STOCK_STATUSES.ID).from(STOCK_STATUSES)
                .where(STOCK_STATUSES.NAME.eq("AVAILABLE")).fetchOne(STOCK_STATUSES.ID);

        dsl.insertInto(USERS).columns(USERS.ID, USERS.USERNAME, USERS.PASSWORD_HASH, USERS.ACTIVE)
                .values(userId, "planner_" + userId, "hash", true).execute();
        dsl.insertInto(USER_ROLES).columns(USER_ROLES.USER_ID, USER_ROLES.ROLE_ID)
                .values(userId, plannerRoleId).execute();
        dsl.insertInto(PRODUCTS).columns(PRODUCTS.ID, PRODUCTS.CODE, PRODUCTS.NAME, PRODUCTS.PRODUCT_TYPE_ID,
                        PRODUCTS.UNIT_ID, PRODUCTS.PRODUCT_STATUS_ID)
                .values(materialProductId, "MAT_" + materialProductId, "Material", rawTypeId, unitId,
                        activeProductStatusId)
                .execute();
        dsl.insertInto(PRODUCTS).columns(PRODUCTS.ID, PRODUCTS.CODE, PRODUCTS.NAME, PRODUCTS.PRODUCT_TYPE_ID,
                        PRODUCTS.UNIT_ID, PRODUCTS.PRODUCT_STATUS_ID)
                .values(finishedProductId, "FG_" + finishedProductId, "Finished", finishedTypeId, unitId,
                        activeProductStatusId)
                .execute();
        dsl.insertInto(WAREHOUSES).columns(WAREHOUSES.ID, WAREHOUSES.CODE, WAREHOUSES.NAME,
                        WAREHOUSES.WAREHOUSE_STATUS_ID)
                .values(warehouseId, "RAW_MATERIAL_WAREHOUSE", "Raw Material Warehouse", activeWarehouseStatusId)
                .execute();
        UUID activeLocationStatusId = dsl.select(fpt.qn.mes.jooq.Tables.LOCATION_STATUSES.ID)
                .from(fpt.qn.mes.jooq.Tables.LOCATION_STATUSES)
                .where(fpt.qn.mes.jooq.Tables.LOCATION_STATUSES.NAME.eq("ACTIVE"))
                .fetchOne(fpt.qn.mes.jooq.Tables.LOCATION_STATUSES.ID);
        dsl.insertInto(WAREHOUSE_LOCATIONS).columns(WAREHOUSE_LOCATIONS.ID, WAREHOUSE_LOCATIONS.WAREHOUSE_ID,
                        WAREHOUSE_LOCATIONS.CODE, WAREHOUSE_LOCATIONS.NAME, WAREHOUSE_LOCATIONS.LOCATION_STATUS_ID)
                .values(locationId, warehouseId, "RAW_LOC_" + locationId, "Raw location", activeLocationStatusId)
                .execute();
        dsl.insertInto(MACHINES).columns(MACHINES.ID, MACHINES.CODE, MACHINES.NAME, MACHINES.MACHINE_STATUS_ID)
                .values(machineId, "M_" + machineId, "Available machine", activeMachineStatusId).execute();
        dsl.insertInto(BOMS).columns(BOMS.ID, BOMS.FINISHED_PRODUCT_ID, BOMS.VERSION, BOMS.BOM_STATUS_ID,
                        BOMS.CREATED_BY)
                .values(bomId, finishedProductId, 1, activeBomStatusId, userId).execute();
        dsl.insertInto(STOCK_LOTS).columns(STOCK_LOTS.ID, STOCK_LOTS.LOT_NUMBER, STOCK_LOTS.PRODUCT_ID)
                .values(lotId, "LOT_" + lotId, materialProductId).execute();
        dsl.insertInto(STOCK_BALANCES).columns(STOCK_BALANCES.ID, STOCK_BALANCES.WAREHOUSE_ID,
                        STOCK_BALANCES.LOCATION_ID, STOCK_BALANCES.PRODUCT_ID, STOCK_BALANCES.LOT_ID,
                        STOCK_BALANCES.STOCK_STATUS_ID, STOCK_BALANCES.QUANTITY)
                .values(UUID.randomUUID(), warehouseId, locationId, materialProductId, lotId,
                        availableStockStatusId, BigDecimal.TEN).execute();
        dsl.insertInto(WORK_ORDERS).columns(WORK_ORDERS.ID, WORK_ORDERS.CODE, WORK_ORDERS.FINISHED_PRODUCT_ID,
                        WORK_ORDERS.BOM_ID, WORK_ORDERS.PLANNED_QUANTITY, WORK_ORDERS.WORK_ORDER_STATUS_ID,
                        WORK_ORDERS.CREATED_BY)
                .values(workOrderId, "WO_" + workOrderId, finishedProductId, bomId, BigDecimal.ONE,
                        plannedStatusId, userId).execute();
        dsl.insertInto(WORK_ORDER_MATERIALS).columns(WORK_ORDER_MATERIALS.ID, WORK_ORDER_MATERIALS.WORK_ORDER_ID,
                        WORK_ORDER_MATERIALS.MATERIAL_PRODUCT_ID, WORK_ORDER_MATERIALS.REQUIRED_QUANTITY,
                        WORK_ORDER_MATERIALS.RESERVED_QUANTITY, WORK_ORDER_MATERIALS.CONSUMED_QUANTITY)
                .values(workOrderMaterialId, workOrderId, materialProductId, BigDecimal.valueOf(5), BigDecimal.ZERO,
                        BigDecimal.ZERO).execute();

        var principal = AppUserPrincipal.builder().id(userId).username("planner_" + userId)
                .roles(java.util.List.of("PLANNER")).enabled(true).build();
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(principal, "n/a",
                        java.util.List.of(new SimpleGrantedAuthority("ROLE_PLANNER"))));
        try {
            var request = new ReserveWorkOrderMaterialsRequest();
            request.setMachineId(machineId);
            var response = workOrderService.reserveMaterials(workOrderId, request);

            assertThat(response.getStatus()).isEqualTo("READY_TO_PRODUCE");
            assertThat(dsl.select(WORK_ORDERS.WORK_ORDER_STATUS_ID).from(WORK_ORDERS)
                    .where(WORK_ORDERS.ID.eq(workOrderId)).fetchOne(WORK_ORDERS.WORK_ORDER_STATUS_ID))
                    .isEqualTo(dsl.select(WORK_ORDER_STATUSES.ID).from(WORK_ORDER_STATUSES)
                            .where(WORK_ORDER_STATUSES.NAME.eq("READY_TO_PRODUCE"))
                            .fetchOne(WORK_ORDER_STATUSES.ID));
            assertThat(dsl.select(STOCK_BALANCES.QUANTITY).from(STOCK_BALANCES)
                    .where(STOCK_BALANCES.WAREHOUSE_ID.eq(warehouseId))
                    .and(STOCK_BALANCES.LOT_ID.eq(lotId))
                    .and(STOCK_BALANCES.STOCK_STATUS_ID.eq(availableStockStatusId))
                    .fetchOne(STOCK_BALANCES.QUANTITY)).isEqualByComparingTo(BigDecimal.valueOf(5));
            assertThat(dsl.selectCount().from(STOCK_MOVEMENTS).where(STOCK_MOVEMENTS.WORK_ORDER_ID.eq(workOrderId))
                    .and(STOCK_MOVEMENTS.LOT_ID.eq(lotId)).fetchOne(0, Integer.class)).isEqualTo(1);
            assertThat(dsl.selectCount().from(AUDIT_LOGS).where(AUDIT_LOGS.ENTITY_ID.eq(workOrderId))
                    .and(AUDIT_LOGS.ACTION.eq("RESERVE_MATERIAL")).fetchOne(0, Integer.class)).isEqualTo(1);
        } finally {
            SecurityContextHolder.clearContext();
        }
    }

    private HttpHeaders jsonHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Content-Type", "application/json");
        return headers;
    }

    private UUID statusId(String name) {
        return dsl.select(WORK_ORDER_STATUSES.ID)
                .from(WORK_ORDER_STATUSES)
                .where(WORK_ORDER_STATUSES.NAME.eq(name))
                .fetchOne(WORK_ORDER_STATUSES.ID);
    }

    private boolean hasActiveTransition(UUID fromStatusId, UUID toStatusId) {
        return dsl.fetchExists(WORK_ORDER_STATUS_TRANSITIONS,
                WORK_ORDER_STATUS_TRANSITIONS.FROM_STATUS_ID.eq(fromStatusId)
                        .and(WORK_ORDER_STATUS_TRANSITIONS.TO_STATUS_ID.eq(toStatusId))
                        .and(WORK_ORDER_STATUS_TRANSITIONS.IS_ACTIVE.isTrue()));
    }
}
