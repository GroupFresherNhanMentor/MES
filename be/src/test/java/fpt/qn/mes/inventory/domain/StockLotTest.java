package fpt.qn.mes.inventory.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import fpt.qn.mes.common.exception.DomainException;
import fpt.qn.mes.inventory.domain.entities.StockLot;

class StockLotTest {

    @Test
    @DisplayName("create with valid parameters returns StockLot instance")
    void create_Success() {
        UUID productId = UUID.randomUUID();
        UUID lotTypeId = UUID.randomUUID();
        LocalDate expiryDate = LocalDate.now().plusMonths(6);

        StockLot lot = StockLot.create("LOT-2026-001", productId, lotTypeId, expiryDate);

        assertThat(lot).isNotNull();
        assertThat(lot.getLotNumber()).isEqualTo("LOT-2026-001");
        assertThat(lot.getProductId()).isEqualTo(productId);
        assertThat(lot.getLotTypeId()).isEqualTo(lotTypeId);
    }

    @Test
    @DisplayName("create with blank lot number throws DomainException")
    void create_BlankLotNumber_ThrowsException() {
        UUID productId = UUID.randomUUID();

        assertThatThrownBy(() -> StockLot.create("   ", productId, null, null))
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("Lot number cannot be empty");
    }

    @Test
    @DisplayName("create with null productId throws DomainException")
    void create_NullProductId_ThrowsException() {
        assertThatThrownBy(() -> StockLot.create("LOT-001", null, null, null))
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("Product ID cannot be null");
    }
}
