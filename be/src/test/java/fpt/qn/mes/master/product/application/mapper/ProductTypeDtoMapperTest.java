package fpt.qn.mes.master.product.application.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import fpt.qn.mes.master.product.application.dto.producttype.ProductTypeResponse;
import fpt.qn.mes.master.product.domain.entities.ProductType;

@ExtendWith(SpringExtension.class)
@Import(ProductTypeDtoMapperImpl.class)
class ProductTypeDtoMapperTest {

    @Autowired
    ProductTypeDtoMapper mapper;

    @Test
    void toDto_mapsAllScalarFields() {
        UUID id = UUID.randomUUID();
        Instant now = Instant.now();

        ProductType type = ProductType.builder()
                .id(id)
                .name("RAW_MATERIAL")
                .description("Raw material")
                .createdAt(now)
                .updatedAt(now)
                .build();

        ProductTypeResponse response = mapper.toDto(type);

        assertThat(response.getId()).isEqualTo(id);
        assertThat(response.getName()).isEqualTo("RAW_MATERIAL");
        assertThat(response.getDescription()).isEqualTo("Raw material");
        assertThat(response.getCreatedAt()).isEqualTo(now);
        assertThat(response.getUpdatedAt()).isEqualTo(now);
    }

    @Test
    void toDto_mapsUserRef_toUserInfo() {
        UUID userId = UUID.randomUUID();
        ProductType.UserRef ref = ProductType.UserRef.builder()
                .id(userId)
                .fullName("Bui Van Thang")
                .username("thangbv")
                .build();

        ProductType type = ProductType.builder()
                .id(UUID.randomUUID())
                .name("RAW_MATERIAL")
                .createdBy(ref)
                .updatedBy(ref)
                .build();

        ProductTypeResponse response = mapper.toDto(type);

        assertThat(response.getCreatedBy()).isNotNull();
        assertThat(response.getCreatedBy().getId()).isEqualTo(userId);
        assertThat(response.getCreatedBy().getFullName()).isEqualTo("Bui Van Thang");
        assertThat(response.getCreatedBy().getUsername()).isEqualTo("thangbv");

        assertThat(response.getUpdatedBy()).isNotNull();
        assertThat(response.getUpdatedBy().getId()).isEqualTo(userId);
    }

    @Test
    void toDto_handlesNullUserRef() {
        ProductType type = ProductType.builder()
                .id(UUID.randomUUID())
                .name("RAW_MATERIAL")
                .createdBy(null)
                .updatedBy(null)
                .build();

        ProductTypeResponse response = mapper.toDto(type);

        assertThat(response.getCreatedBy()).isNull();
        assertThat(response.getUpdatedBy()).isNull();
    }
}
