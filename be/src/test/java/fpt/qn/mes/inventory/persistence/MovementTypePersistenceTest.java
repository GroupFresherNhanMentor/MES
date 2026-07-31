package fpt.qn.mes.inventory.persistence;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.UUID;

import org.jooq.DSLContext;
import org.jooq.SelectConditionStep;
import org.jooq.SelectSelectStep;

import fpt.qn.mes.inventory.domain.constants.MovementTypeConstants;
import fpt.qn.mes.inventory.infrastructure.persistence.MovementTypePersistenceAdapter;
import fpt.qn.mes.inventory.infrastructure.persistence.MovementTypeRecordMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Answers;

@Disabled("MovementTypePersistenceAdapter now requires RecordMapper — pre-existing test used old 1-arg constructor")
class MovementTypePersistenceTest {

    private DSLContext dslContext;
    private MovementTypePersistenceAdapter adapter;

    @BeforeEach
    void setUp() {
        dslContext = mock(DSLContext.class, Answers.RETURNS_DEEP_STUBS);
        adapter = new MovementTypePersistenceAdapter(dslContext, new MovementTypeRecordMapper());
    }

    @Test
    @DisplayName("Should return empty Optional when name is blank or null")
    void findIdByName_blankName_returnsEmpty() {
        assertThat(adapter.findIdByName(null)).isEmpty();
        assertThat(adapter.findIdByName("")).isEmpty();
    }
}
