package fpt.qn.mes.master.machine.application.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import fpt.qn.mes.master.machine.application.dto.machine.MachineResponse;
import fpt.qn.mes.master.machine.domain.entities.Machine;
import fpt.qn.mes.master.machine.domain.entities.Machine.ProductionLineRef;
import fpt.qn.mes.master.machine.domain.entities.Machine.UserRef;
import fpt.qn.mes.master.machine.domain.entities.MachineStatus;

@ExtendWith(SpringExtension.class)
@Import({
    MachineDtoMapperImpl.class,
    MachineStatusDtoMapperImpl.class
})
class MachineDtoMapperTest {

    @Autowired
    MachineDtoMapper mapper;

    @Test
    void toDto_mapsAllScalarFields() {
        UUID id = UUID.randomUUID();
        UUID createdById = UUID.randomUUID();
        UserRef createdBy = UserRef.builder().id(createdById).fullName("Test User").username("testuser").build();
        Instant now = Instant.now();

        Machine machine = Machine.builder()
                .id(id)
                .code("MCH-001")
                .name("CNC Machine")
                .createdBy(createdBy)
                .createdAt(now)
                .updatedAt(now)
                .build();

        MachineResponse response = mapper.toDto(machine);

        assertThat(response.getId()).isEqualTo(id);
        assertThat(response.getCode()).isEqualTo("MCH-001");
        assertThat(response.getName()).isEqualTo("CNC Machine");
        assertThat(response.getCreatedBy()).isNotNull();
        assertThat(response.getCreatedBy().getId()).isEqualTo(createdById);
        assertThat(response.getCreatedBy().getFullName()).isEqualTo("Test User");
        assertThat(response.getCreatedAt()).isEqualTo(now);
        assertThat(response.getUpdatedAt()).isEqualTo(now);
    }

    @Test
    void toDto_mapsProductionLineRef_toProductionLineInfo() {
        UUID lineId = UUID.randomUUID();
        ProductionLineRef lineRef = ProductionLineRef.builder()
                .id(lineId)
                .code("LINE-A")
                .name("Assembly Line A")
                .build();

        Machine machine = Machine.builder()
                .id(UUID.randomUUID())
                .code("MCH-001")
                .name("CNC Machine")
                .productionLine(lineRef)
                .build();

        MachineResponse response = mapper.toDto(machine);

        assertThat(response.getProductionLine()).isNotNull();
        assertThat(response.getProductionLine().getId()).isEqualTo(lineId);
        assertThat(response.getProductionLine().getCode()).isEqualTo("LINE-A");
        assertThat(response.getProductionLine().getName()).isEqualTo("Assembly Line A");
    }

    @Test
    void toDto_mapsMachineStatus() {
        UUID statusId = UUID.randomUUID();
        MachineStatus status = MachineStatus.builder()
                .id(statusId)
                .name("ACTIVE")
                .description("Machine is active")
                .build();

        Machine machine = Machine.builder()
                .id(UUID.randomUUID())
                .code("MCH-001")
                .name("CNC Machine")
                .machineStatus(status)
                .build();

        MachineResponse response = mapper.toDto(machine);

        assertThat(response.getMachineStatus()).isNotNull();
        assertThat(response.getMachineStatus().getId()).isEqualTo(statusId);
        assertThat(response.getMachineStatus().getName()).isEqualTo("ACTIVE");
        assertThat(response.getMachineStatus().getDescription()).isEqualTo("Machine is active");
    }

    @Test
    void toDto_handlesNullNestedEntities() {
        Machine machine = Machine.builder()
                .id(UUID.randomUUID())
                .code("MCH-001")
                .name("CNC Machine")
                .productionLine(null)
                .machineStatus(null)
                .build();

        MachineResponse response = mapper.toDto(machine);

        assertThat(response.getProductionLine()).isNull();
        assertThat(response.getMachineStatus()).isNull();
    }
}
