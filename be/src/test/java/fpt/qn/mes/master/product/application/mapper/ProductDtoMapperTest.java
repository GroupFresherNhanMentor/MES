package fpt.qn.mes.master.product.application.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import fpt.qn.mes.master.product.application.dto.product.ProductResponse;
import fpt.qn.mes.master.product.domain.entities.Product;
import fpt.qn.mes.master.product.domain.entities.ProductStatus;
import fpt.qn.mes.master.product.domain.entities.ProductType;
import fpt.qn.mes.master.product.domain.entities.UnitOfMeasure;

@ExtendWith(SpringExtension.class)
@Import({
    ProductDtoMapperImpl.class,
    ProductTypeDtoMapperImpl.class,
    UnitOfMeasureDtoMapperImpl.class,
    ProductStatusDtoMapperImpl.class
})
class ProductDtoMapperTest {

    @Autowired
    ProductDtoMapper mapper;

    @Test
    void toDto_mapsAllScalarFields() {
        UUID id = UUID.randomUUID();
        Instant now = Instant.now();

        Product product = Product.builder()
                .id(id)
                .code("PRD-001")
                .name("Steel Rod")
                .version("1.0")
                .createdAt(now)
                .updatedAt(now)
                .build();

        ProductResponse response = mapper.toDto(product);

        assertThat(response.getId()).isEqualTo(id);
        assertThat(response.getCode()).isEqualTo("PRD-001");
        assertThat(response.getName()).isEqualTo("Steel Rod");
        assertThat(response.getVersion()).isEqualTo("1.0");
        assertThat(response.getCreatedAt()).isEqualTo(now);
        assertThat(response.getUpdatedAt()).isEqualTo(now);
    }

    @Test
    void toDto_mapsNestedEntities() {
        UUID productTypeId = UUID.randomUUID();
        UUID unitId = UUID.randomUUID();
        UUID statusId = UUID.randomUUID();

        Product product = Product.builder()
                .id(UUID.randomUUID())
                .code("PRD-001")
                .name("Steel Rod")
                .productType(ProductType.builder().id(productTypeId).name("RAW_MATERIAL").build())
                .unit(UnitOfMeasure.builder().id(unitId).name("KG").build())
                .productStatus(ProductStatus.builder().id(statusId).name("ACTIVE").build())
                .build();

        ProductResponse response = mapper.toDto(product);

        assertThat(response.getProductType()).isNotNull();
        assertThat(response.getProductType().getId()).isEqualTo(productTypeId);
        assertThat(response.getProductType().getName()).isEqualTo("RAW_MATERIAL");

        assertThat(response.getUnit()).isNotNull();
        assertThat(response.getUnit().getId()).isEqualTo(unitId);
        assertThat(response.getUnit().getName()).isEqualTo("KG");

        assertThat(response.getProductStatus()).isNotNull();
        assertThat(response.getProductStatus().getId()).isEqualTo(statusId);
        assertThat(response.getProductStatus().getName()).isEqualTo("ACTIVE");
    }

    @Test
    void toDto_mapsUserRef_toUserInfo() {
        UUID userId = UUID.randomUUID();
        Product.UserRef ref = Product.UserRef.builder()
                .id(userId)
                .fullName("Bui Van Thang")
                .username("thangbv")
                .build();

        Product product = Product.builder()
                .id(UUID.randomUUID())
                .code("PRD-001")
                .name("Steel Rod")
                .createdBy(ref)
                .updatedBy(ref)
                .build();

        ProductResponse response = mapper.toDto(product);

        assertThat(response.getCreatedBy()).isNotNull();
        assertThat(response.getCreatedBy().getId()).isEqualTo(userId);
        assertThat(response.getCreatedBy().getFullName()).isEqualTo("Bui Van Thang");
        assertThat(response.getCreatedBy().getUsername()).isEqualTo("thangbv");

        assertThat(response.getUpdatedBy()).isNotNull();
        assertThat(response.getUpdatedBy().getId()).isEqualTo(userId);
    }

    @Test
    void toDto_handlesNullNestedEntities() {
        Product product = Product.builder()
                .id(UUID.randomUUID())
                .code("PRD-001")
                .name("Steel Rod")
                .productType(null)
                .unit(null)
                .productStatus(null)
                .createdBy(null)
                .updatedBy(null)
                .build();

        ProductResponse response = mapper.toDto(product);

        assertThat(response.getProductType()).isNull();
        assertThat(response.getUnit()).isNull();
        assertThat(response.getProductStatus()).isNull();
        assertThat(response.getCreatedBy()).isNull();
        assertThat(response.getUpdatedBy()).isNull();
    }
}
