package fpt.qn.mes.master.product.infrastructure.persistence;

import java.time.ZoneOffset;

import org.springframework.stereotype.Component;

import fpt.qn.mes.jooq.tables.records.ProductTypesRecord;
import fpt.qn.mes.master.product.domain.entities.ProductType;

@Component
public class ProductTypeRecordMapper {

    public ProductType toDomain(ProductTypesRecord r) {
        return ProductType.builder()
            .id(r.getId())
            .name(r.getName())
            .description(r.getDescription())
            .createdBy(r.getCreatedBy())
            .updatedBy(r.getUpdatedBy())
            .createdAt(r.getCreatedAt() != null ? r.getCreatedAt().toInstant() : null)
            .updatedAt(r.getUpdatedAt() != null ? r.getUpdatedAt().toInstant() : null)
            .build();
    }

    public ProductTypesRecord toRecord(ProductType type) {
        ProductTypesRecord r = new ProductTypesRecord();
        r.setId(type.getId());
        r.setName(type.getName());
        r.setDescription(type.getDescription());
        r.setCreatedBy(type.getCreatedBy());
        r.setUpdatedBy(type.getUpdatedBy());
        if (type.getCreatedAt() != null) r.setCreatedAt(type.getCreatedAt().atOffset(ZoneOffset.UTC));
        if (type.getUpdatedAt() != null) r.setUpdatedAt(type.getUpdatedAt().atOffset(ZoneOffset.UTC));
        return r;
    }
}
