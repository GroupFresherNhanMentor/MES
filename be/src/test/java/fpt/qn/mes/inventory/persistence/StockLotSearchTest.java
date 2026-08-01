package fpt.qn.mes.inventory.persistence;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import fpt.qn.mes.inventory.domain.repository.criteria.StockLotSearchCriteria;

class StockLotSearchTest {

    @Test
    @DisplayName("StockLotSearchCriteria builds with expected fields")
    void stockLotSearchCriteria_buildsCorrectly() {
        StockLotSearchCriteria criteria = StockLotSearchCriteria.builder()
                .lotNumber("LOT123")
                .page(0)
                .size(10)
                .build();

        assertThat(criteria.getLotNumber()).isEqualTo("LOT123");
        assertThat(criteria.getPage()).isZero();
        assertThat(criteria.getSize()).isEqualTo(10);
    }

    @Test
    @DisplayName("StockLotSearchCriteria defaults are page 0 and size 20 when not set")
    void stockLotSearchCriteria_defaultPagination() {
        StockLotSearchCriteria criteria = StockLotSearchCriteria.builder().build();

        assertThat(criteria.getLotNumber()).isNull();
        assertThat(criteria.getProductId()).isNull();
    }
}
