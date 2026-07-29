package fpt.qn.mes.inventory.domain;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import fpt.qn.mes.inventory.domain.entities.LotType;
import fpt.qn.mes.inventory.domain.entities.MovementType;
import fpt.qn.mes.inventory.domain.entities.StockStatus;

class ValueObjectsTest {

    @Test
    @DisplayName("StockStatus builder and getters verify correctness")
    void stockStatus_Correctness() {
        UUID id = UUID.randomUUID();
        StockStatus status = StockStatus.builder()
                .id(id)
                .name("AVAILABLE")
                .description("Stock available for issue")
                .build();

        assertThat(status.getId()).isEqualTo(id);
        assertThat(status.getName()).isEqualTo("AVAILABLE");
        assertThat(status.getDescription()).isEqualTo("Stock available for issue");
    }

    @Test
    @DisplayName("LotType builder and getters verify correctness")
    void lotType_Correctness() {
        UUID id = UUID.randomUUID();
        LotType lotType = LotType.builder()
                .id(id)
                .name("RAW_MATERIAL")
                .description("Raw material lot")
                .build();

        assertThat(lotType.getId()).isEqualTo(id);
        assertThat(lotType.getName()).isEqualTo("RAW_MATERIAL");
    }

    @Test
    @DisplayName("MovementType builder and getters verify correctness")
    void movementType_Correctness() {
        UUID id = UUID.randomUUID();
        MovementType movementType = MovementType.builder()
                .id(id)
                .name("RECEIPT")
                .description("Goods receipt movement")
                .build();

        assertThat(movementType.getId()).isEqualTo(id);
        assertThat(movementType.getName()).isEqualTo("RECEIPT");
    }
}
