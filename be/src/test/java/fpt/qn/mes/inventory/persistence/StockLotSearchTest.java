package fpt.qn.mes.inventory.persistence;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;

import org.jooq.DSLContext;
import org.jooq.OrderField;
import org.jooq.Result;
import org.jooq.Table;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Answers;

import fpt.qn.mes.common.dto.response.PageResponse;
import fpt.qn.mes.inventory.domain.entities.StockLot;
import fpt.qn.mes.inventory.domain.repository.criteria.StockLotSearchCriteria;
import fpt.qn.mes.inventory.infrastructure.persistence.InventoryRecordMapper;
import fpt.qn.mes.inventory.infrastructure.persistence.StockLotPersistenceAdapter;

class StockLotSearchTest {

    private DSLContext dslContext;
    private InventoryRecordMapper mapper;
    private StockLotPersistenceAdapter adapter;

    @BeforeEach
    void setUp() {
        dslContext = mock(DSLContext.class, Answers.RETURNS_DEEP_STUBS);
        mapper = mock(InventoryRecordMapper.class);
        adapter = new StockLotPersistenceAdapter(dslContext, mapper);
    }

    @Test
    @DisplayName("Should return empty PageResponse when fetchCount is zero for StockLot search")
    void search_returnsEmptyPageResponse() {
        StockLotSearchCriteria criteria = StockLotSearchCriteria.builder()
                .lotNumber("LOT123")
                .page(0)
                .size(10)
                .build();

        when(dslContext.fetchCount(any(Table.class), any(List.class))).thenReturn(0);
        when(dslContext.selectFrom(any(Table.class))
                .where(any(List.class))
                .orderBy((OrderField<?>) any())
                .limit(anyInt())
                .offset(anyInt())
                .fetch()).thenReturn(mock(Result.class));

        List<StockLot> response = adapter.search(criteria);

        assertThat(response).isNotNull();
        assertThat(response).isEmpty();
    }
}
