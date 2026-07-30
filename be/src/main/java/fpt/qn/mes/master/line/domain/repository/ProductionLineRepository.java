package fpt.qn.mes.master.line.domain.repository;

import java.util.Optional;
import java.util.UUID;

import fpt.qn.mes.common.domainQuery.PaginationResult;
import fpt.qn.mes.master.line.domain.entities.ProductionLine;

public interface ProductionLineRepository {
    Optional<ProductionLine> findById(UUID id);
    java.util.List<ProductionLine> findByIds(java.util.Collection<UUID> ids);
    ProductionLine save(ProductionLine line);
    ProductionLine update(ProductionLine line);
    void deleteById(UUID id);
    PaginationResult<ProductionLine> findAll(int page, int size);
    PaginationResult<ProductionLine> findAllByStatus(int page, int size, UUID statusId);
    boolean existsByCode(String code);
}
