package fpt.qn.mes.master.product.domain.repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import fpt.qn.mes.common.domainQuery.PaginationResult;
import fpt.qn.mes.master.product.domain.entities.Product;

public interface ProductRepository {
    Optional<Product> findById(UUID id);
    List<Product> findByIds(Collection<UUID> ids);
    Product save(Product product);
    Product update(Product product);
    void deleteById(UUID id);
    PaginationResult<Product> findAll(int page, int size);
    PaginationResult<Product> findAllByStatus(int page, int size, UUID statusId);
    boolean existsByCode(String code);
}
