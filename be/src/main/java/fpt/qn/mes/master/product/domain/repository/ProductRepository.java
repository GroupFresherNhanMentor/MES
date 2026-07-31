package fpt.qn.mes.master.product.domain.repository;

import java.util.Optional;
import java.util.UUID;

import fpt.qn.mes.common.domainQuery.PaginationResult;
import fpt.qn.mes.master.product.domain.entities.Product;
import fpt.qn.mes.master.product.domain.repository.criteria.ProductSearchCriteria;

public interface ProductRepository {
    Optional<Product> findById(UUID id);
    Product save(Product product);
    Product update(Product product);
    void deleteById(UUID id);
    PaginationResult<Product> search(ProductSearchCriteria criteria);
    boolean existsByCode(String code);
    boolean existsByVersion(String version);
}
