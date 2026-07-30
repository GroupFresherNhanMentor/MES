package fpt.qn.mes.master.product.application.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import fpt.qn.mes.master.product.application.dto.productstatus.ProductStatusResponse;
import fpt.qn.mes.master.product.domain.entities.ProductStatus;

@ExtendWith(SpringExtension.class)
@Import(ProductStatusDtoMapperImpl.class)
class ProductStatusDtoMapperTest {

    @Autowired
    ProductStatusDtoMapper mapper;

    @Test
    void toDto_mapsAllScalarFields() {
        UUID id = UUID.randomUUID();
        Instant now = Instant.now();

        ProductStatus status = ProductStatus.builder()
                .id(id)
                .name("ACTIVE")
                .description("Active status")
                .createdAt(now)
                .updatedAt(now)
                .build();

        ProductStatusResponse response = mapper.toDto(status);

        assertThat(response.getId()).isEqualTo(id);
        assertThat(response.getName()).isEqualTo("ACTIVE");
        assertThat(response.getDescription()).isEqualTo("Active status");
        assertThat(response.getCreatedAt()).isEqualTo(now);
        assertThat(response.getUpdatedAt()).isEqualTo(now);
    }

    @Test
    void toDto_mapsUserRef_toUserInfo() {
        UUID userId = UUID.randomUUID();
        ProductStatus.UserRef ref = ProductStatus.UserRef.builder()
                .id(userId)
                .fullName("Bui Van Thang")
                .username("thangbv")
                .build();

        ProductStatus status = ProductStatus.builder()
                .id(UUID.randomUUID())
                .name("ACTIVE")
                .createdBy(ref)
                .updatedBy(ref)
                .build();

        ProductStatusResponse response = mapper.toDto(status);

        assertThat(response.getCreatedBy()).isNotNull();
        assertThat(response.getCreatedBy().getId()).isEqualTo(userId);
        assertThat(response.getCreatedBy().getFullName()).isEqualTo("Bui Van Thang");
        assertThat(response.getCreatedBy().getUsername()).isEqualTo("thangbv");

        assertThat(response.getUpdatedBy()).isNotNull();
        assertThat(response.getUpdatedBy().getId()).isEqualTo(userId);
    }

    @Test
    void toDto_handlesNullUserRef() {
        ProductStatus status = ProductStatus.builder()
                .id(UUID.randomUUID())
                .name("ACTIVE")
                .createdBy(null)
                .updatedBy(null)
                .build();

        ProductStatusResponse response = mapper.toDto(status);

        assertThat(response.getCreatedBy()).isNull();
        assertThat(response.getUpdatedBy()).isNull();
    }
}
