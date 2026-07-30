package fpt.qn.mes.master.product.infrastructure.persistence;

import java.time.ZoneOffset;

import org.springframework.stereotype.Component;

import fpt.qn.mes.jooq.tables.records.ProductStatusesRecord;
import fpt.qn.mes.master.product.domain.entities.ProductStatus;

@Component
public class ProductStatusRecordMapper {

    public ProductStatus toDomain(ProductStatusesRecord r) {
        return ProductStatus.builder()
            .id(r.getId())
            .name(r.getName())
            .description(r.getDescription())
            .createdBy(r.getCreatedBy())
            .updatedBy(r.getUpdatedBy())
            .createdAt(r.getCreatedAt() != null ? r.getCreatedAt().toInstant() : null)
            .updatedAt(r.getUpdatedAt() != null ? r.getUpdatedAt().toInstant() : null)
            .build();
    }

    public ProductStatusesRecord toRecord(ProductStatus status) {
        ProductStatusesRecord r = new ProductStatusesRecord();
        r.setId(status.getId());
        r.setName(status.getName());
        r.setDescription(status.getDescription());
        r.setCreatedBy(status.getCreatedBy());
        r.setUpdatedBy(status.getUpdatedBy());
        if (status.getCreatedAt() != null) r.setCreatedAt(status.getCreatedAt().atOffset(ZoneOffset.UTC));
        if (status.getUpdatedAt() != null) r.setUpdatedAt(status.getUpdatedAt().atOffset(ZoneOffset.UTC));
        return r;
    }
}
