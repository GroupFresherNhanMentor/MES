package fpt.qn.mes.master.product.infrastructure.persistence;

import static fpt.qn.mes.jooq.Tables.PRODUCTS;

import java.util.Optional;
import java.util.UUID;

import org.jooq.DSLContext;
import org.springframework.stereotype.Repository;

import fpt.qn.mes.common.dto.PaginationResult;
import fpt.qn.mes.common.repository.BaseRepository;
import fpt.qn.mes.jooq.tables.records.ProductsRecord;
import fpt.qn.mes.master.product.domain.entities.Product;
import fpt.qn.mes.master.product.domain.repository.ProductRepository;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;

@Repository
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ProductPersistenceAdapter extends BaseRepository<ProductsRecord> implements ProductRepository {

    ProductRecordMapper mapper;

    public ProductPersistenceAdapter(DSLContext ctx, ProductRecordMapper mapper) {
        super(ctx, PRODUCTS); this.mapper = mapper;
    }

    @Override public Optional<Product> findById(UUID id) { throw new UnsupportedOperationException("Not implemented"); }
    @Override public Product save(Product product) { throw new UnsupportedOperationException("Not implemented"); }
    @Override public Product update(Product product) { throw new UnsupportedOperationException("Not implemented"); }
    @Override public void deleteById(UUID id) {}
    @Override public PaginationResult<Product> findAll(int page, int size) { throw new UnsupportedOperationException("Not implemented"); }
    @Override public boolean existsByCode(String code) { throw new UnsupportedOperationException("Not implemented"); }
}
