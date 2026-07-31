package fpt.qn.mes.master.product.infrastructure.persistence.producttype;

import java.time.ZoneOffset;

import org.springframework.stereotype.Component;

import fpt.qn.mes.jooq.tables.records.ProductTypesRecord;
import fpt.qn.mes.jooq.tables.records.UsersRecord;
import fpt.qn.mes.master.product.domain.entities.ProductType;

@Component
public class ProductTypeRecordMapper {

    public ProductType toDomain(ProductTypesRecord r, UsersRecord creator, UsersRecord updater) {
        return ProductType.builder()
            .id(r.getId())
            .name(r.getName())
            .description(r.getDescription())
            .createdBy(creator.getId() != null
                ? ProductType.UserRef.builder().id(creator.getId()).fullName(creator.getFullName()).username(creator.getUsername()).build()
                : null)
            .updatedBy(updater.getId() != null
                ? ProductType.UserRef.builder().id(updater.getId()).fullName(updater.getFullName()).username(updater.getUsername()).build()
                : null)
            .createdAt(r.getCreatedAt().toInstant())
            .updatedAt(r.getUpdatedAt() != null ? r.getUpdatedAt().toInstant() : null)
            .build();
    }

    public ProductTypesRecord toRecord(ProductType type) {
        ProductTypesRecord r = new ProductTypesRecord();
        r.setId(type.getId());
        r.setName(type.getName());
        r.setDescription(type.getDescription());
        r.setCreatedBy(type.getCreatedBy() != null ? type.getCreatedBy().getId() : null);
        r.setUpdatedBy(type.getUpdatedBy() != null ? type.getUpdatedBy().getId() : null);
        r.setCreatedAt(type.getCreatedAt().atOffset(ZoneOffset.UTC));
        r.setUpdatedAt(type.getUpdatedAt() != null ? type.getUpdatedAt().atOffset(ZoneOffset.UTC) : null);
        return r;
    }
}
