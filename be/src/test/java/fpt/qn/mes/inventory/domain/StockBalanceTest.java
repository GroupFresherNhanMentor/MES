package fpt.qn.mes.inventory.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import fpt.qn.mes.inventory.domain.entities.StockBalance;
import fpt.qn.mes.inventory.domain.entities.StockStatus;

class StockBalanceTest {

    @Test
    @DisplayName("deductQuantity with valid amount reduces balance quantity")
    void deductQuantity_Success() {
        StockStatus status = StockStatus.builder().id(UUID.randomUUID()).name("AVAILABLE").build();
        StockBalance balance = StockBalance.builder()
                .id(UUID.randomUUID())
                .stockStatus(status)
                .quantity(new BigDecimal("100.00"))
                .build();

        balance.deductQuantity(new BigDecimal("40.00"));

        assertThat(balance.getQuantity()).isEqualTo(new BigDecimal("60.00"));
    }

    @Test
    @DisplayName("deductQuantity exceeding on-hand balance throws IllegalArgumentException")
    void deductQuantity_ExceedingOnHand_ThrowsException() {
        StockBalance balance = StockBalance.builder()
                .id(UUID.randomUUID())
                .quantity(new BigDecimal("30.00"))
                .build();

        assertThatThrownBy(() -> balance.deductQuantity(new BigDecimal("50.00")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Insufficient stock balance");
    }

    @Test
    @DisplayName("deductQuantity with zero or negative amount throws IllegalArgumentException")
    void deductQuantity_InvalidAmount_ThrowsException() {
        StockBalance balance = StockBalance.builder()
                .id(UUID.randomUUID())
                .quantity(new BigDecimal("30.00"))
                .build();

        assertThatThrownBy(() -> balance.deductQuantity(BigDecimal.ZERO))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Deduction quantity must be positive");
    }

    @Test
    @DisplayName("addQuantity increases balance quantity")
    void addQuantity_Success() {
        StockBalance balance = StockBalance.builder()
                .id(UUID.randomUUID())
                .quantity(new BigDecimal("30.00"))
                .build();

        balance.addQuantity(new BigDecimal("20.00"));

        assertThat(balance.getQuantity()).isEqualTo(new BigDecimal("50.00"));
    }
}
