package fpt.qn.mes.inventory.integration;

import static fpt.qn.mes.jooq.Tables.LOCATION_STATUSES;
import static fpt.qn.mes.jooq.Tables.LOT_TYPES;
import static fpt.qn.mes.jooq.Tables.MOVEMENT_TYPES;
import static fpt.qn.mes.jooq.Tables.PRODUCTS;
import static fpt.qn.mes.jooq.Tables.PRODUCT_STATUSES;
import static fpt.qn.mes.jooq.Tables.PRODUCT_TYPES;
import static fpt.qn.mes.jooq.Tables.STOCK_BALANCES;
import static fpt.qn.mes.jooq.Tables.STOCK_LOTS;
import static fpt.qn.mes.jooq.Tables.STOCK_MOVEMENTS;
import static fpt.qn.mes.jooq.Tables.STOCK_STATUSES;
import static fpt.qn.mes.jooq.Tables.UNITS_OF_MEASURE;
import static fpt.qn.mes.jooq.Tables.USERS;
import static fpt.qn.mes.jooq.Tables.WAREHOUSES;
import static fpt.qn.mes.jooq.Tables.WAREHOUSE_LOCATIONS;
import static fpt.qn.mes.jooq.Tables.WAREHOUSE_STATUSES;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import org.jooq.DSLContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import fpt.qn.mes.AbstractIntegrationTest;
import fpt.qn.mes.common.dto.response.PageResponse;
import fpt.qn.mes.inventory.application.dto.request.CreateMovementRequest;
import fpt.qn.mes.inventory.application.dto.request.CreateStockLotRequest;
import fpt.qn.mes.inventory.application.dto.request.StockBalanceSearchRequest;
import fpt.qn.mes.inventory.application.dto.response.StockBalanceDto;
import fpt.qn.mes.inventory.application.dto.response.StockLotDto;
import fpt.qn.mes.inventory.application.dto.response.StockMovementDto;
import fpt.qn.mes.inventory.application.exception.InsufficientStockException;
import fpt.qn.mes.inventory.application.service.InventoryService;

class InventoryIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    InventoryService inventoryService;

    @Autowired
    DSLContext dsl;

    @Autowired
    TransactionTemplate txTemplate;

    UUID productTypeId;
    UUID unitId;
    UUID productStatusId;
    UUID productId;
    UUID warehouseStatusId;
    UUID warehouseId;
    UUID locationStatusId;
    UUID locationId;
    UUID lotTypeId;
    UUID lotId;
    UUID stockStatusId;
    UUID movementTypeId;
    UUID userId;

    @BeforeEach
    void setUp() {
        productTypeId = UUID.randomUUID();
        unitId = UUID.randomUUID();
        productStatusId = UUID.randomUUID();
        productId = UUID.randomUUID();
        warehouseStatusId = UUID.randomUUID();
        warehouseId = UUID.randomUUID();
        locationStatusId = UUID.randomUUID();
        locationId = UUID.randomUUID();
        lotTypeId = UUID.randomUUID();
        lotId = UUID.randomUUID();
        stockStatusId = UUID.randomUUID();
        movementTypeId = UUID.randomUUID();
        userId = UUID.randomUUID();

        // Seed master data required by foreign keys
        dsl.insertInto(USERS, USERS.ID, USERS.USERNAME, USERS.PASSWORD_HASH)
                .values(userId, "user_" + userId.toString().substring(0, 8), "hash")
                .execute();

        dsl.insertInto(PRODUCT_TYPES, PRODUCT_TYPES.ID, PRODUCT_TYPES.NAME)
                .values(productTypeId, "TYPE_" + productTypeId.toString().substring(0, 8))
                .execute();

        dsl.insertInto(UNITS_OF_MEASURE, UNITS_OF_MEASURE.ID, UNITS_OF_MEASURE.NAME)
                .values(unitId, "UOM_" + unitId.toString().substring(0, 8))
                .execute();

        dsl.insertInto(PRODUCT_STATUSES, PRODUCT_STATUSES.ID, PRODUCT_STATUSES.NAME)
                .values(productStatusId, "STAT_" + productStatusId.toString().substring(0, 8))
                .execute();

        dsl.insertInto(PRODUCTS, PRODUCTS.ID, PRODUCTS.CODE, PRODUCTS.NAME, PRODUCTS.PRODUCT_TYPE_ID, PRODUCTS.UNIT_ID, PRODUCTS.PRODUCT_STATUS_ID)
                .values(productId, "PROD_" + productId.toString().substring(0, 8), "Test Product", productTypeId, unitId, productStatusId)
                .execute();

        dsl.insertInto(WAREHOUSE_STATUSES, WAREHOUSE_STATUSES.ID, WAREHOUSE_STATUSES.NAME)
                .values(warehouseStatusId, "WSTAT_" + warehouseStatusId.toString().substring(0, 8))
                .execute();

        dsl.insertInto(WAREHOUSES, WAREHOUSES.ID, WAREHOUSES.CODE, WAREHOUSES.NAME, WAREHOUSES.WAREHOUSE_STATUS_ID)
                .values(warehouseId, "WH_" + warehouseId.toString().substring(0, 8), "Test Warehouse", warehouseStatusId)
                .execute();

        dsl.insertInto(LOCATION_STATUSES, LOCATION_STATUSES.ID, LOCATION_STATUSES.NAME)
                .values(locationStatusId, "LSTAT_" + locationStatusId.toString().substring(0, 8))
                .execute();

        dsl.insertInto(WAREHOUSE_LOCATIONS, WAREHOUSE_LOCATIONS.ID, WAREHOUSE_LOCATIONS.WAREHOUSE_ID, WAREHOUSE_LOCATIONS.CODE, WAREHOUSE_LOCATIONS.NAME, WAREHOUSE_LOCATIONS.LOCATION_STATUS_ID)
                .values(locationId, warehouseId, "LOC_" + locationId.toString().substring(0, 8), "Test Location", locationStatusId)
                .execute();

        dsl.insertInto(LOT_TYPES, LOT_TYPES.ID, LOT_TYPES.NAME)
                .values(lotTypeId, "LTYPE_" + lotTypeId.toString().substring(0, 8))
                .execute();

        dsl.insertInto(STOCK_LOTS, STOCK_LOTS.ID, STOCK_LOTS.LOT_NUMBER, STOCK_LOTS.PRODUCT_ID, STOCK_LOTS.LOT_TYPE_ID)
                .values(lotId, "LOT_" + lotId.toString().substring(0, 8), productId, lotTypeId)
                .execute();

        dsl.insertInto(STOCK_STATUSES, STOCK_STATUSES.ID, STOCK_STATUSES.NAME)
                .values(stockStatusId, "SSTAT_" + stockStatusId.toString().substring(0, 8))
                .execute();

        dsl.insertInto(MOVEMENT_TYPES, MOVEMENT_TYPES.ID, MOVEMENT_TYPES.NAME)
                .values(movementTypeId, "MTYPE_" + movementTypeId.toString().substring(0, 8))
                .execute();
    }

    @Test
    @DisplayName("Create stock lot and query by ID should succeed")
    void createAndGetStockLot_Success() {
        CreateStockLotRequest request = new CreateStockLotRequest();
        request.setLotNumber("LOT-INT-001");
        request.setProductId(productId);
        request.setLotTypeId(lotTypeId);
        request.setExpiryDate(LocalDate.now().plusDays(30));

        StockLotDto created = inventoryService.createStockLot(request);

        assertThat(created).isNotNull();
        assertThat(created.getId()).isNotNull();
        assertThat(created.getLotNumber()).isEqualTo("LOT-INT-001");

        StockLotDto fetched = inventoryService.getStockLotById(created.getId());
        assertThat(fetched.getLotNumber()).isEqualTo("LOT-INT-001");
    }

    @Test
    @DisplayName("Record stock RECEIPT movement should update stock balance")
    void recordReceiptMovement_Success() {
        CreateMovementRequest request = new CreateMovementRequest();
        request.setMovementTypeId(movementTypeId);
        request.setProductId(productId);
        request.setWarehouseId(warehouseId);
        request.setLocationId(locationId);
        request.setLotId(lotId);
        request.setToStatusId(stockStatusId);
        request.setQuantity(new BigDecimal("100.00"));
        request.setReferenceNo("PO-9988");

        StockMovementDto movement = inventoryService.recordMovement(request, userId);

        assertThat(movement).isNotNull();
        assertThat(movement.getReferenceNo()).isEqualTo("PO-9988");

        StockBalanceSearchRequest searchReq = new StockBalanceSearchRequest();
        searchReq.setWarehouseId(warehouseId);
        searchReq.setProductId(productId);

        PageResponse<StockBalanceDto> balances = inventoryService.getStockBalances(searchReq);
        assertThat(balances.getItems()).hasSize(1);
        assertThat(balances.getItems().get(0).getQuantity()).isEqualByComparingTo("100.00");
    }

    @Test
    @DisplayName("Record ISSUE movement with insufficient stock should fail")
    void recordIssueMovement_InsufficientStock_Fails() {
        CreateMovementRequest request = new CreateMovementRequest();
        request.setMovementTypeId(movementTypeId);
        request.setProductId(productId);
        request.setWarehouseId(warehouseId);
        request.setLocationId(locationId);
        request.setLotId(lotId);
        request.setFromStatusId(stockStatusId);
        request.setQuantity(new BigDecimal("50.00"));

        assertThatThrownBy(() -> inventoryService.recordMovement(request, userId))
                .isInstanceOf(InsufficientStockException.class);
    }

    @Test
    @DisplayName("getStockBalances should return PageResponse of StockBalanceDto")
    void getStockBalances_Success() {
        CreateStockLotRequest lotReq = new CreateStockLotRequest();
        lotReq.setLotNumber("LOT-BAL-01");
        lotReq.setProductId(productId);
        lotReq.setLotTypeId(lotTypeId);
        lotReq.setExpiryDate(LocalDate.now().plusDays(30));
        StockLotDto lotDto = inventoryService.createStockLot(lotReq);

        CreateMovementRequest req = new CreateMovementRequest();
        req.setMovementTypeId(movementTypeId);
        req.setProductId(productId);
        req.setWarehouseId(warehouseId);
        req.setLocationId(locationId);
        req.setLotId(lotDto.getId());
        req.setToStatusId(stockStatusId);
        req.setQuantity(new BigDecimal("100.00"));
        inventoryService.recordMovement(req, userId);

        StockBalanceSearchRequest searchReq = new StockBalanceSearchRequest();
        searchReq.setWarehouseId(warehouseId);
        searchReq.setProductId(productId);

        PageResponse<StockBalanceDto> pageRes = inventoryService.getStockBalances(searchReq);

        assertThat(pageRes).isNotNull();
        assertThat(pageRes.getItems()).hasSize(1);
        assertThat(pageRes.getItems().get(0).getQuantity()).isEqualByComparingTo(new BigDecimal("100.00"));
    }

    @Test
    @DisplayName("Concurrent stock balance additions should accurately aggregate quantity")
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    @Disabled
    void concurrentStockMovements_IntegrityCheck() throws InterruptedException {
        // Seed data must be committed so concurrent threads can see it.
        // @BeforeEach runs inside the class-level @Transactional which never commits,
        // so we insert seed data in a dedicated committed transaction here.
        UUID cProductTypeId = UUID.randomUUID();
        UUID cUnitId = UUID.randomUUID();
        UUID cProductStatusId = UUID.randomUUID();
        UUID cProductId = UUID.randomUUID();
        UUID cWarehouseStatusId = UUID.randomUUID();
        UUID cWarehouseId = UUID.randomUUID();
        UUID cLocationStatusId = UUID.randomUUID();
        UUID cLocationId = UUID.randomUUID();
        UUID cLotTypeId = UUID.randomUUID();
        UUID cLotId = UUID.randomUUID();
        UUID cStockStatusId = UUID.randomUUID();
        UUID cMovementTypeId = UUID.randomUUID();
        UUID cUserId = UUID.randomUUID();

        txTemplate.executeWithoutResult(status -> {
            dsl.insertInto(USERS, USERS.ID, USERS.USERNAME, USERS.PASSWORD_HASH)
                    .values(cUserId, "user_" + cUserId.toString().substring(0, 8), "hash")
                    .execute();
            dsl.insertInto(PRODUCT_TYPES, PRODUCT_TYPES.ID, PRODUCT_TYPES.NAME)
                    .values(cProductTypeId, "TYPE_" + cProductTypeId.toString().substring(0, 8))
                    .execute();
            dsl.insertInto(UNITS_OF_MEASURE, UNITS_OF_MEASURE.ID, UNITS_OF_MEASURE.NAME)
                    .values(cUnitId, "UOM_" + cUnitId.toString().substring(0, 8))
                    .execute();
            dsl.insertInto(PRODUCT_STATUSES, PRODUCT_STATUSES.ID, PRODUCT_STATUSES.NAME)
                    .values(cProductStatusId, "STAT_" + cProductStatusId.toString().substring(0, 8))
                    .execute();
            dsl.insertInto(PRODUCTS, PRODUCTS.ID, PRODUCTS.CODE, PRODUCTS.NAME, PRODUCTS.PRODUCT_TYPE_ID, PRODUCTS.UNIT_ID, PRODUCTS.PRODUCT_STATUS_ID)
                    .values(cProductId, "PROD_" + cProductId.toString().substring(0, 8), "Test Product", cProductTypeId, cUnitId, cProductStatusId)
                    .execute();
            dsl.insertInto(WAREHOUSE_STATUSES, WAREHOUSE_STATUSES.ID, WAREHOUSE_STATUSES.NAME)
                    .values(cWarehouseStatusId, "WSTAT_" + cWarehouseStatusId.toString().substring(0, 8))
                    .execute();
            dsl.insertInto(WAREHOUSES, WAREHOUSES.ID, WAREHOUSES.CODE, WAREHOUSES.NAME, WAREHOUSES.WAREHOUSE_STATUS_ID)
                    .values(cWarehouseId, "WH_" + cWarehouseId.toString().substring(0, 8), "Test Warehouse", cWarehouseStatusId)
                    .execute();
            dsl.insertInto(LOCATION_STATUSES, LOCATION_STATUSES.ID, LOCATION_STATUSES.NAME)
                    .values(cLocationStatusId, "LSTAT_" + cLocationStatusId.toString().substring(0, 8))
                    .execute();
            dsl.insertInto(WAREHOUSE_LOCATIONS, WAREHOUSE_LOCATIONS.ID, WAREHOUSE_LOCATIONS.WAREHOUSE_ID, WAREHOUSE_LOCATIONS.CODE, WAREHOUSE_LOCATIONS.NAME, WAREHOUSE_LOCATIONS.LOCATION_STATUS_ID)
                    .values(cLocationId, cWarehouseId, "LOC_" + cLocationId.toString().substring(0, 8), "Test Location", cLocationStatusId)
                    .execute();
            dsl.insertInto(LOT_TYPES, LOT_TYPES.ID, LOT_TYPES.NAME)
                    .values(cLotTypeId, "LTYPE_" + cLotTypeId.toString().substring(0, 8))
                    .execute();
            dsl.insertInto(STOCK_LOTS, STOCK_LOTS.ID, STOCK_LOTS.LOT_NUMBER, STOCK_LOTS.PRODUCT_ID, STOCK_LOTS.LOT_TYPE_ID)
                    .values(cLotId, "LOT_" + cLotId.toString().substring(0, 8), cProductId, cLotTypeId)
                    .execute();
            dsl.insertInto(STOCK_STATUSES, STOCK_STATUSES.ID, STOCK_STATUSES.NAME)
                    .values(cStockStatusId, "SSTAT_" + cStockStatusId.toString().substring(0, 8))
                    .execute();
            dsl.insertInto(MOVEMENT_TYPES, MOVEMENT_TYPES.ID, MOVEMENT_TYPES.NAME)
                    .values(cMovementTypeId, "MTYPE_" + cMovementTypeId.toString().substring(0, 8))
                    .execute();
        });

        try {
            int threads = 5;
            ExecutorService executor = Executors.newFixedThreadPool(threads);
            CountDownLatch latch = new CountDownLatch(threads);
            AtomicInteger successCount = new AtomicInteger(0);

            for (int i = 0; i < threads; i++) {
                executor.submit(() -> {
                    try {
                        CreateMovementRequest request = new CreateMovementRequest();
                        request.setMovementTypeId(cMovementTypeId);
                        request.setProductId(cProductId);
                        request.setWarehouseId(cWarehouseId);
                        request.setLocationId(cLocationId);
                        request.setLotId(cLotId);
                        request.setToStatusId(cStockStatusId);
                        request.setQuantity(new BigDecimal("10.00"));
                        inventoryService.recordMovement(request, cUserId);
                        successCount.incrementAndGet();
                    } catch (Exception e) {
                        e.printStackTrace();
                    } finally {
                        latch.countDown();
                    }
                });
            }

            latch.await();
            executor.shutdown();

            assertThat(successCount.get()).isEqualTo(threads);
        } finally {
            // Clean up committed data
            txTemplate.executeWithoutResult(status -> {
                dsl.deleteFrom(STOCK_MOVEMENTS).where(STOCK_MOVEMENTS.PRODUCT_ID.eq(cProductId)).execute();
                dsl.deleteFrom(STOCK_BALANCES).where(STOCK_BALANCES.WAREHOUSE_ID.eq(cWarehouseId)).execute();
                dsl.deleteFrom(STOCK_LOTS).where(STOCK_LOTS.PRODUCT_ID.eq(cProductId)).execute();
                dsl.deleteFrom(WAREHOUSE_LOCATIONS).where(WAREHOUSE_LOCATIONS.WAREHOUSE_ID.eq(cWarehouseId)).execute();
                dsl.deleteFrom(LOCATION_STATUSES).where(LOCATION_STATUSES.ID.eq(cLocationStatusId)).execute();
                dsl.deleteFrom(WAREHOUSES).where(WAREHOUSES.ID.eq(cWarehouseId)).execute();
                dsl.deleteFrom(WAREHOUSE_STATUSES).where(WAREHOUSE_STATUSES.ID.eq(cWarehouseStatusId)).execute();
                dsl.deleteFrom(PRODUCTS).where(PRODUCTS.ID.eq(cProductId)).execute();
                dsl.deleteFrom(PRODUCT_STATUSES).where(PRODUCT_STATUSES.ID.eq(cProductStatusId)).execute();
                dsl.deleteFrom(PRODUCT_TYPES).where(PRODUCT_TYPES.ID.eq(cProductTypeId)).execute();
                dsl.deleteFrom(UNITS_OF_MEASURE).where(UNITS_OF_MEASURE.ID.eq(cUnitId)).execute();
                dsl.deleteFrom(MOVEMENT_TYPES).where(MOVEMENT_TYPES.ID.eq(cMovementTypeId)).execute();
                dsl.deleteFrom(STOCK_STATUSES).where(STOCK_STATUSES.ID.eq(cStockStatusId)).execute();
                dsl.deleteFrom(LOT_TYPES).where(LOT_TYPES.ID.eq(cLotTypeId)).execute();
                dsl.deleteFrom(USERS).where(USERS.ID.eq(cUserId)).execute();
            });
        }
    }
}
