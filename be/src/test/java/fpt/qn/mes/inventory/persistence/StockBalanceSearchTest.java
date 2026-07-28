package fpt.qn.mes.inventory.persistence;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.UUID;

import org.jooq.DSLContext;
import org.jooq.OrderField;
import org.jooq.Result;
import org.jooq.Table;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Answers;

import fpt.qn.mes.common.dto.response.PageResponse;
import fpt.qn.mes.inventory.domain.entities.StockBalance;
import fpt.qn.mes.inventory.domain.repository.criteria.StockBalanceSearchCriteria;
import fpt.qn.mes.inventory.infrastructure.persistence.InventoryRecordMapper;
import fpt.qn.mes.inventory.infrastructure.persistence.StockBalancePersistenceAdapter;

class StockBalanceSearchTest {

    private DSLContext dslContext;
    private InventoryRecordMapper mapper;
    private StockBalancePersistenceAdapter adapter;

    @BeforeEach
    void setUp() {
        dslContext = mock(DSLContext.class, Answers.RETURNS_DEEP_STUBS);
        mapper = mock(InventoryRecordMapper.class);
        adapter = new StockBalancePersistenceAdapter(dslContext, mapper);
    }

    @Test
    @DisplayName("Should return empty PageResponse when fetchCount is zero")
    void search_returnsEmptyPageResponse() {
        UUID warehouseId = UUID.randomUUID();
        StockBalanceSearchCriteria criteria = StockBalanceSearchCriteria.builder()
                .warehouseId(warehouseId)
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

        PageResponse<StockBalance> response = adapter.search(criteria);

        assertThat(response).isNotNull();
        assertThat(response.getPageNumber()).isEqualTo(0);
        assertThat(response.getPageSize()).isEqualTo(10);
        assertThat(response.getTotalElements()).isEqualTo(0L);
        assertThat(response.getItems()).isEmpty();
    }
}
