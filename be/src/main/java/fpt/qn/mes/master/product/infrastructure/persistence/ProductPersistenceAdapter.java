package fpt.qn.mes.master.product.infrastructure.persistence;

import static fpt.qn.mes.jooq.Tables.PRODUCTS;

import java.util.Optional;
import java.util.UUID;

import org.jooq.DSLContext;
import org.springframework.stereotype.Repository;

import fpt.qn.mes.common.dto.response.PaginationResult;
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

    @Override
    public Optional<Product> findById(UUID id) {
        return fetchById(id).map(mapper::toDomain);
    }

    @Override
    public Product save(Product product) {
        ProductsRecord record = mapper.toRecord(product);
        ProductsRecord saved = create(record);
        return mapper.toDomain(saved);
    }

    @Override
    public Product update(Product product) {
        ProductsRecord record = mapper.toRecord(product);
        ProductsRecord updated = update(record);
        return mapper.toDomain(updated);
    }

    @Override
    public void deleteById(UUID id) {
        // soft delete only — no-op
    }

    @Override
    public PaginationResult<Product> findAll(int page, int size) {
        var records = ctx.selectFrom(PRODUCTS)
                .orderBy(PRODUCTS.CREATED_AT.desc())
                .limit(size)
                .offset((long) page * size)
                .fetch();
        int total = ctx.fetchCount(ctx.selectFrom(PRODUCTS));
        var items = records.stream().map(mapper::toDomain).toList();
        return PaginationResult.of(items, total, page, size);
    }

    @Override
    public boolean existsByCode(String code) {
        return ctx.fetchExists(
                ctx.selectFrom(PRODUCTS).where(PRODUCTS.CODE.eq(code)));
    }
}
