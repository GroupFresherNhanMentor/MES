package fpt.qn.mes.workorder.infrastructure.persistence;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import fpt.qn.mes.workorder.domain.entities.WorkOrderEvent;

class WorkOrderRecordMapperTest {

    WorkOrderRecordMapper mapper = new WorkOrderRecordMapper();

    @Test
    void workOrderEventRoundTrip_preservesSchemaFields() {
        Instant eventTimestamp = Instant.parse("2026-08-01T10:15:30Z");
        WorkOrderEvent event = WorkOrderEvent.builder()
                .id(UUID.randomUUID())
                .workOrderId(UUID.randomUUID())
                .productionRunId(UUID.randomUUID())
                .eventTypeId(UUID.randomUUID())
                .operatorId(UUID.randomUUID())
                .eventTimestamp(eventTimestamp)
                .note("Production started")
                .build();

        var record = mapper.toRecord(event);
        var mappedEvent = mapper.toDomain(record);

        assertThat(mappedEvent.getId()).isEqualTo(event.getId());
        assertThat(mappedEvent.getWorkOrderId()).isEqualTo(event.getWorkOrderId());
        assertThat(mappedEvent.getProductionRunId()).isEqualTo(event.getProductionRunId());
        assertThat(mappedEvent.getEventTypeId()).isEqualTo(event.getEventTypeId());
        assertThat(mappedEvent.getOperatorId()).isEqualTo(event.getOperatorId());
        assertThat(mappedEvent.getEventTimestamp()).isEqualTo(eventTimestamp);
        assertThat(mappedEvent.getNote()).isEqualTo(event.getNote());
    }

    @Test
    void nullWorkOrderEvent_mapsToNull() {
        assertThat(mapper.toRecord((WorkOrderEvent) null)).isNull();
    }
}
