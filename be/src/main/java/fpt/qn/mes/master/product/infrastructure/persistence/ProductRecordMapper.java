package fpt.qn.mes.master.product.infrastructure.persistence;

import org.springframework.stereotype.Component;

import fpt.qn.mes.jooq.tables.records.ProductsRecord;
import fpt.qn.mes.master.product.domain.entities.Product;

@Component
public class ProductRecordMapper {

    public Product toDomain(ProductsRecord r) {
        return null;
    }

    public ProductsRecord toRecord(Product p) {
        return null;
    }
}
