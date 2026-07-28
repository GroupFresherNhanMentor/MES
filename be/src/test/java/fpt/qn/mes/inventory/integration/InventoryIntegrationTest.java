package fpt.qn.mes.inventory.integration;

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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import fpt.qn.mes.AbstractIntegrationTest;
import fpt.qn.mes.inventory.application.dto.request.CreateMovementRequest;
import fpt.qn.mes.inventory.application.dto.request.CreateStockLotRequest;
import fpt.qn.mes.inventory.application.dto.response.StockBalanceDto;
import fpt.qn.mes.inventory.application.dto.response.StockLotDto;
import fpt.qn.mes.inventory.application.dto.response.StockMovementDto;
import fpt.qn.mes.inventory.application.exception.InsufficientStockException;
import fpt.qn.mes.inventory.application.service.InventoryService;

class InventoryIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    InventoryService inventoryService;

    UUID productId;
    UUID warehouseId;
    UUID locationId;
    UUID userId;

    @BeforeEach
    void setUp() {
        productId = UUID.randomUUID();
        warehouseId = UUID.randomUUID();
        locationId = UUID.randomUUID();
        userId = UUID.randomUUID();
    }

    @Test
    @DisplayName("Create stock lot and query by ID should succeed")
    void createAndGetStockLot_Success() {
        CreateStockLotRequest request = new CreateStockLotRequest();
        request.setLotNumber("LOT-INT-001");
        request.setProductId(productId);
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
        request.setMovementTypeId(UUID.randomUUID());
        request.setProductId(productId);
        request.setWarehouseId(warehouseId);
        request.setLocationId(locationId);
        request.setQuantity(new BigDecimal("100.00"));
        request.setReferenceNo("PO-9988");

        StockMovementDto movement = inventoryService.recordMovement(request, userId);

        assertThat(movement).isNotNull();
        assertThat(movement.getReferenceNo()).isEqualTo("PO-9988");

        List<StockBalanceDto> balances = inventoryService.getStockBalances(warehouseId, productId);
        assertThat(balances).hasSize(1);
        assertThat(balances.get(0).getQuantity()).isEqualByComparingTo("100.00");
    }

    @Test
    @DisplayName("Record ISSUE movement with insufficient stock should fail")
    void recordIssueMovement_InsufficientStock_Fails() {
        CreateMovementRequest request = new CreateMovementRequest();
        request.setMovementTypeId(UUID.randomUUID());
        request.setProductId(productId);
        request.setWarehouseId(warehouseId);
        request.setLocationId(locationId);
        request.setQuantity(new BigDecimal("50.00"));
        request.setFromStatusId(UUID.randomUUID());

        assertThatThrownBy(() -> inventoryService.recordMovement(request, userId))
                .isInstanceOf(InsufficientStockException.class);
    }

    @Test
    @DisplayName("Concurrent stock balance additions should accurately aggregate quantity")
    void concurrentStockMovements_IntegrityCheck() throws InterruptedException {
        int threads = 5;
        ExecutorService executor = Executors.newFixedThreadPool(threads);
        CountDownLatch latch = new CountDownLatch(threads);
        AtomicInteger successCount = new AtomicInteger(0);

        for (int i = 0; i < threads; i++) {
            executor.submit(() -> {
                try {
                    CreateMovementRequest request = new CreateMovementRequest();
                    request.setMovementTypeId(UUID.randomUUID());
                    request.setProductId(productId);
                    request.setWarehouseId(warehouseId);
                    request.setLocationId(locationId);
                    request.setQuantity(new BigDecimal("10.00"));
                    inventoryService.recordMovement(request, userId);
                    successCount.incrementAndGet();
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
        executor.shutdown();

        assertThat(successCount.get()).isEqualTo(threads);
    }
}
