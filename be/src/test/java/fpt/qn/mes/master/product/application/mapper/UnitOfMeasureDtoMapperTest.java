package fpt.qn.mes.master.product.application.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import fpt.qn.mes.master.product.application.dto.unitofmeasure.UnitOfMeasureResponse;
import fpt.qn.mes.master.product.domain.entities.UnitOfMeasure;

@ExtendWith(SpringExtension.class)
@Import(UnitOfMeasureDtoMapperImpl.class)
class UnitOfMeasureDtoMapperTest {

    @Autowired
    UnitOfMeasureDtoMapper mapper;

    @Test
    void toDto_mapsAllScalarFields() {
        UUID id = UUID.randomUUID();
        Instant now = Instant.now();

        UnitOfMeasure unit = UnitOfMeasure.builder()
                .id(id)
                .name("KG")
                .description("Kilogram")
                .createdAt(now)
                .updatedAt(now)
                .build();

        UnitOfMeasureResponse response = mapper.toDto(unit);

        assertThat(response.getId()).isEqualTo(id);
        assertThat(response.getName()).isEqualTo("KG");
        assertThat(response.getDescription()).isEqualTo("Kilogram");
        assertThat(response.getCreatedAt()).isEqualTo(now);
        assertThat(response.getUpdatedAt()).isEqualTo(now);
    }

    @Test
    void toDto_mapsUserRef_toUserInfo() {
        UUID userId = UUID.randomUUID();
        UnitOfMeasure.UserRef ref = UnitOfMeasure.UserRef.builder()
                .id(userId)
                .fullName("Bui Van Thang")
                .username("thangbv")
                .build();

        UnitOfMeasure unit = UnitOfMeasure.builder()
                .id(UUID.randomUUID())
                .name("KG")
                .createdBy(ref)
                .updatedBy(ref)
                .build();

        UnitOfMeasureResponse response = mapper.toDto(unit);

        assertThat(response.getCreatedBy()).isNotNull();
        assertThat(response.getCreatedBy().getId()).isEqualTo(userId);
        assertThat(response.getCreatedBy().getFullName()).isEqualTo("Bui Van Thang");
        assertThat(response.getCreatedBy().getUsername()).isEqualTo("thangbv");

        assertThat(response.getUpdatedBy()).isNotNull();
        assertThat(response.getUpdatedBy().getId()).isEqualTo(userId);
    }

    @Test
    void toDto_handlesNullUserRef() {
        UnitOfMeasure unit = UnitOfMeasure.builder()
                .id(UUID.randomUUID())
                .name("KG")
                .createdBy(null)
                .updatedBy(null)
                .build();

        UnitOfMeasureResponse response = mapper.toDto(unit);

        assertThat(response.getCreatedBy()).isNull();
        assertThat(response.getUpdatedBy()).isNull();
    }
}
