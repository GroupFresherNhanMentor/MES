package fpt.qn.mes.workorder.integration;

import static fpt.qn.mes.jooq.Tables.AUDIT_LOGS;
import static fpt.qn.mes.jooq.Tables.BOM_STATUSES;
import static fpt.qn.mes.jooq.Tables.BOMS;
import static fpt.qn.mes.jooq.Tables.MACHINE_STATUSES;
import static fpt.qn.mes.jooq.Tables.MACHINES;
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
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
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
import fpt.qn.mes.workorder.application.service.WorkOrderService;

class WorkOrderIntegrationTest extends AbstractIntegrationTest {

    @LocalServerPort
    int port;

    RestTemplate restTemplate;

    @Autowired
    DSLContext dsl;

    @Autowired
    WorkOrderService workOrderService;

    @BeforeEach
    void setUp() {
        restTemplate = new RestTemplate();
        seedAdminUser();
    }

    private String url() {
        // Base test endpoint URL updated to /api/work-orders
        return "http://localhost:" + port + "/api/work-orders/"
                + UUID.randomUUID() + "/reserve-materials";
    }

    @Test
    void reserveMaterials_returns401WithoutAuthentication() {
        // Reserve materials without authentication headers should return 401 UNAUTHORIZED
        var request = new HttpEntity<>(jsonHeaders());

        assertThatThrownBy(() -> restTemplate.exchange(url(), HttpMethod.POST, request, String.class))
                .isInstanceOf(HttpStatusCodeException.class)
                .satisfies(error -> assertThat(((HttpStatusCodeException) error).getStatusCode())
                        .isEqualTo(HttpStatus.UNAUTHORIZED));
    }

    @Test
    void reserveMaterials_returns403ForNonPlanner() {
        // Reserve materials with non-planner user token should return 403 FORBIDDEN
        HttpHeaders headers = jsonHeaders();
        headers.setBearerAuth(generateAdminToken());

        assertThatThrownBy(() -> restTemplate.exchange(url(), HttpMethod.POST,
                new HttpEntity<>(headers), String.class))
                .isInstanceOf(HttpStatusCodeException.class)
                .satisfies(error -> assertThat(((HttpStatusCodeException) error).getStatusCode())
                        .isEqualTo(HttpStatus.FORBIDDEN));
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
            // Reserve materials for Work Order without passing request body
            var response = workOrderService.reserveMaterials(workOrderId);

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
}
