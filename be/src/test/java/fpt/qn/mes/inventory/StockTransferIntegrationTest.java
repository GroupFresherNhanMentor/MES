package fpt.qn.mes.inventory;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import fpt.qn.mes.inventory.application.dto.stockmovement.create.StockTransferRequest;
import fpt.qn.mes.inventory.application.dto.response.StockTransferResponse;

class StockTransferIntegrationTest {

    @Test
    @DisplayName("StockTransferRequest structure validation")
    void testStockTransferRequestValidation() {
        UUID fromWh = UUID.randomUUID();
        UUID fromLoc = UUID.randomUUID();
        UUID toWh = UUID.randomUUID();
        UUID toLoc = UUID.randomUUID();
        UUID prodId = UUID.randomUUID();
        UUID lotId = UUID.randomUUID();

        StockTransferRequest request = StockTransferRequest.builder()
                .fromWarehouseId(fromWh)
                .fromLocationId(fromLoc)
                .toWarehouseId(toWh)
                .toLocationId(toLoc)
                .productId(prodId)
                .lotId(lotId)
                .quantity(new BigDecimal("20.00"))
                .build();

        assertThat(request.getQuantity()).isEqualTo(new BigDecimal("20.00"));
        assertThat(request.getFromLocationId()).isNotEqualTo(request.getToLocationId());
    }

    @Test
    @DisplayName("StockTransferResponse structure validation")
    void testStockTransferResponseValidation() {
        StockTransferResponse response = StockTransferResponse.builder().build();
        assertThat(response).isNotNull();
    }
}
