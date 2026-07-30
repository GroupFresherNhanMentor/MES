package fpt.qn.mes.master.product.domain.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import fpt.qn.mes.common.domainQuery.PaginationResult;
import fpt.qn.mes.master.product.domain.entities.ProductStatus;
import fpt.qn.mes.master.product.domain.repository.criteria.ProductStatusSearchCriteria;

public interface ProductStatusRepository {
    Optional<ProductStatus> findById(UUID id);
    List<ProductStatus> findAll();
    ProductStatus save(ProductStatus status);
    PaginationResult<ProductStatus> search(ProductStatusSearchCriteria criteria);
}
