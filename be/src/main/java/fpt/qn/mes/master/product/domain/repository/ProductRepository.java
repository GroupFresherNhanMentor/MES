package fpt.qn.mes.master.product.domain.repository;

import java.util.Optional;
import java.util.UUID;

import fpt.qn.mes.common.dto.PaginationResult;
import fpt.qn.mes.master.product.domain.entities.Product;

public interface ProductRepository {
    Optional<Product> findById(UUID id);
    Product save(Product product);
    Product update(Product product);
    void deleteById(UUID id);
    PaginationResult<Product> findAll(int page, int size);
    boolean existsByCode(String code);
}
