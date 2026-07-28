package fpt.qn.mes.master.product.infrastructure.persistence;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;

import org.springframework.stereotype.Component;

import fpt.qn.mes.jooq.tables.records.ProductsRecord;
import fpt.qn.mes.master.product.domain.entities.Product;

@Component
public class ProductRecordMapper {

    public Product toDomain(ProductsRecord r) {
        if (r == null) return null;
        return Product.builder()
                .id(r.getId())
                .code(r.getCode())
                .name(r.getName())
                .productTypeId(r.getProductTypeId())
                .unitId(r.getUnitId())
                .productStatusId(r.getProductStatusId())
                .version(r.getVersion())
                .createdAt(r.getCreatedAt().toInstant())
                .createdBy(r.getCreatedBy())
                .updatedAt(r.getUpdatedAt().toInstant())
                .updatedBy(r.getUpdatedBy())
                .build();
    }

    public ProductsRecord toRecord(Product p) {
        ProductsRecord r = new ProductsRecord();
        r.setId(p.getId());
        r.setCode(p.getCode());
        r.setName(p.getName());
        r.setProductTypeId(p.getProductTypeId());
        r.setUnitId(p.getUnitId());
        r.setProductStatusId(p.getProductStatusId());
        r.setVersion(p.getVersion());
        r.setCreatedAt(OffsetDateTime.ofInstant(p.getCreatedAt(), ZoneOffset.UTC));
        r.setCreatedBy(p.getCreatedBy());
        r.setUpdatedAt(OffsetDateTime.ofInstant(p.getUpdatedAt(), ZoneOffset.UTC));
        r.setUpdatedBy(p.getUpdatedBy());
        return r;
    }
}
