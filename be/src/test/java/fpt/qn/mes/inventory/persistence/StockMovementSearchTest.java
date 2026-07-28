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
import fpt.qn.mes.inventory.domain.entities.StockMovement;
import fpt.qn.mes.inventory.domain.repository.criteria.StockMovementSearchCriteria;
import fpt.qn.mes.inventory.infrastructure.persistence.InventoryRecordMapper;
import fpt.qn.mes.inventory.infrastructure.persistence.StockMovementPersistenceAdapter;

class StockMovementSearchTest {

    private DSLContext dslContext;
    private InventoryRecordMapper mapper;
    private StockMovementPersistenceAdapter adapter;

    @BeforeEach
    void setUp() {
        dslContext = mock(DSLContext.class, Answers.RETURNS_DEEP_STUBS);
        mapper = mock(InventoryRecordMapper.class);
        adapter = new StockMovementPersistenceAdapter(dslContext, mapper);
    }

    @Test
    @DisplayName("Should return empty PageResponse when fetchCount is zero for StockMovement search")
    void search_returnsEmptyPageResponse() {
        StockMovementSearchCriteria criteria = StockMovementSearchCriteria.builder()
                .referenceNo("REF001")
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

        List<StockMovement> response = adapter.search(criteria);

        assertThat(response).isNotNull();
        assertThat(response).isEmpty();
    }
}
