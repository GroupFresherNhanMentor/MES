package fpt.qn.mes.master.product.domain.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import fpt.qn.mes.common.domainQuery.PaginationResult;
import fpt.qn.mes.master.product.domain.entities.ProductType;
import fpt.qn.mes.master.product.domain.repository.criteria.ProductTypeSearchCriteria;

public interface ProductTypeRepository {
    Optional<ProductType> findById(UUID id);
    List<ProductType> findAll();
    ProductType save(ProductType type);
    PaginationResult<ProductType> search(ProductTypeSearchCriteria criteria);
}
