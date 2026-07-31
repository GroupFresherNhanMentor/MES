package fpt.qn.mes.master.product.infrastructure.persistence.product;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;

import org.springframework.stereotype.Component;

import fpt.qn.mes.jooq.tables.records.ProductStatusesRecord;
import fpt.qn.mes.jooq.tables.records.ProductTypesRecord;
import fpt.qn.mes.jooq.tables.records.ProductsRecord;
import fpt.qn.mes.jooq.tables.records.UnitsOfMeasureRecord;
import fpt.qn.mes.jooq.tables.records.UsersRecord;
import fpt.qn.mes.master.product.domain.entities.Product;
import fpt.qn.mes.master.product.domain.entities.ProductStatus;
import fpt.qn.mes.master.product.domain.entities.ProductType;
import fpt.qn.mes.master.product.domain.entities.UnitOfMeasure;

@Component
public class ProductRecordMapper {

    public Product toDomain(ProductsRecord prod, ProductTypesRecord type, UnitsOfMeasureRecord uom, ProductStatusesRecord status, UsersRecord creator, UsersRecord updater) {
        return Product.builder()
                .id(prod.getId())
                .code(prod.getCode())
                .name(prod.getName())
                .version(prod.getVersion())
                .productType(type.getId() != null
                        ? ProductType.builder().id(type.getId()).name(type.getName()).description(type.getDescription()).build()
                        : null)
                .unit(uom.getId() != null
                        ? UnitOfMeasure.builder().id(uom.getId()).name(uom.getName()).description(uom.getDescription()).build()
                        : null)
                .productStatus(status.getId() != null
                        ? ProductStatus.builder().id(status.getId()).name(status.getName()).description(status.getDescription()).build()
                        : null)
                .createdAt(prod.getCreatedAt().toInstant())
                .createdBy(creator.getId() != null
                        ? Product.UserRef.builder().id(creator.getId()).fullName(creator.getFullName()).username(creator.getUsername()).build()
                        : null)
                .updatedAt(prod.getUpdatedAt() != null ? prod.getUpdatedAt().toInstant() : null)
                .updatedBy(updater.getId() != null
                        ? Product.UserRef.builder().id(updater.getId()).fullName(updater.getFullName()).username(updater.getUsername()).build()
                        : null)
                .build();
    }

    public ProductsRecord toRecord(Product p) {
        ProductsRecord r = new ProductsRecord();
        r.setId(p.getId());
        r.setCode(p.getCode());
        r.setName(p.getName());
        r.setVersion(p.getVersion());
        r.setProductTypeId(p.getProductType().getId());
        r.setUnitId(p.getUnit().getId());
        r.setProductStatusId(p.getProductStatus().getId());
        r.setCreatedAt(OffsetDateTime.ofInstant(p.getCreatedAt(), ZoneOffset.UTC));
        r.setCreatedBy(p.getCreatedBy() != null ? p.getCreatedBy().getId() : null);
        r.setUpdatedAt(p.getUpdatedAt() != null ? OffsetDateTime.ofInstant(p.getUpdatedAt(), ZoneOffset.UTC) : null);
        r.setUpdatedBy(p.getUpdatedBy() != null ? p.getUpdatedBy().getId() : null);
        return r;
    }
}
