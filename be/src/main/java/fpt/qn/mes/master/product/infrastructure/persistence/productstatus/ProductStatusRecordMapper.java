package fpt.qn.mes.master.product.infrastructure.persistence.productstatus;

import java.time.ZoneOffset;

import org.springframework.stereotype.Component;

import fpt.qn.mes.jooq.tables.records.ProductStatusesRecord;
import fpt.qn.mes.jooq.tables.records.UsersRecord;
import fpt.qn.mes.master.product.domain.entities.ProductStatus;

@Component
public class ProductStatusRecordMapper {

    public ProductStatus toDomain(ProductStatusesRecord r, UsersRecord creator, UsersRecord updater) {
        return ProductStatus.builder()
            .id(r.getId())
            .name(r.getName())
            .description(r.getDescription())
            .createdBy(creator.getId() != null
                ? ProductStatus.UserRef.builder().id(creator.getId()).fullName(creator.getFullName()).username(creator.getUsername()).build()
                : null)
            .updatedBy(updater.getId() != null
                ? ProductStatus.UserRef.builder().id(updater.getId()).fullName(updater.getFullName()).username(updater.getUsername()).build()
                : null)
            .createdAt(r.getCreatedAt().toInstant())
            .updatedAt(r.getUpdatedAt() != null ? r.getUpdatedAt().toInstant() : null)
            .build();
    }

    public ProductStatusesRecord toRecord(ProductStatus status) {
        ProductStatusesRecord r = new ProductStatusesRecord();
        r.setId(status.getId());
        r.setName(status.getName());
        r.setDescription(status.getDescription());
        r.setCreatedBy(status.getCreatedBy() != null ? status.getCreatedBy().getId() : null);
        r.setUpdatedBy(status.getUpdatedBy() != null ? status.getUpdatedBy().getId() : null);
        r.setCreatedAt(status.getCreatedAt().atOffset(ZoneOffset.UTC));
        r.setUpdatedAt(status.getUpdatedAt() != null ? status.getUpdatedAt().atOffset(ZoneOffset.UTC) : null);
        return r;
    }
}
