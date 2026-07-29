package fpt.qn.mes.inventory.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import fpt.qn.mes.common.exception.DomainException;
import fpt.qn.mes.inventory.domain.entities.StockMovement;

class StockMovementTest {

    @Test
    @DisplayName("create with valid parameters returns StockMovement instance")
    void create_Success() {
        UUID movementTypeId = UUID.randomUUID();
        UUID productId = UUID.randomUUID();
        UUID warehouseId = UUID.randomUUID();
        UUID createdBy = UUID.randomUUID();

        StockMovement movement = StockMovement.create(
                movementTypeId, productId, null, warehouseId, null,
                new BigDecimal("50.00"), null, null, "PO-10001", "Goods receipt", createdBy
        );

        assertThat(movement).isNotNull();
        assertThat(movement.getQuantity()).isEqualTo(new BigDecimal("50.00"));
        assertThat(movement.getProductId()).isEqualTo(productId);
    }

    @Test
    @DisplayName("create with zero or negative quantity throws DomainException")
    void create_ZeroQuantity_ThrowsException() {
        UUID productId = UUID.randomUUID();
        UUID warehouseId = UUID.randomUUID();
        UUID createdBy = UUID.randomUUID();

        assertThatThrownBy(() -> StockMovement.create(
                null, productId, null, warehouseId, null,
                BigDecimal.ZERO, null, null, null, null, createdBy
        ))
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("Movement quantity must be strictly positive");
    }

    @Test
    @DisplayName("create with null required metadata throws DomainException")
    void create_NullMetadata_ThrowsException() {
        UUID productId = UUID.randomUUID();
        UUID warehouseId = UUID.randomUUID();

        assertThatThrownBy(() -> StockMovement.create(
                null, productId, null, warehouseId, null,
                new BigDecimal("10.00"), null, null, null, null, null
        ))
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("Created-by user ID cannot be null");
    }
}
