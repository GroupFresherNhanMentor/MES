package fpt.qn.mes.master.line.domain.repository;

import java.util.Optional;
import java.util.UUID;

import fpt.qn.mes.common.dto.response.PaginationResult;
import fpt.qn.mes.master.line.domain.entities.ProductionLine;

public interface ProductionLineRepository {
    Optional<ProductionLine> findById(UUID id);
    ProductionLine save(ProductionLine line);
    ProductionLine update(ProductionLine line);
    void deleteById(UUID id);
    PaginationResult<ProductionLine> findAll(int page, int size);
    boolean existsByCode(String code);
}
