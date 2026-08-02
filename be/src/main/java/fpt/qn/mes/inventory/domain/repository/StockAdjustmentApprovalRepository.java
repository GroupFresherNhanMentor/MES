package fpt.qn.mes.inventory.domain.repository;

import java.util.Optional;
import java.util.UUID;

import fpt.qn.mes.common.domainQuery.PaginationResult;
import fpt.qn.mes.inventory.domain.entities.StockAdjustmentApproval;
import fpt.qn.mes.inventory.domain.repository.criteria.StockAdjustmentApprovalSearchCriteria;

public interface StockAdjustmentApprovalRepository {

    Optional<StockAdjustmentApproval> findById(UUID id);

    StockAdjustmentApproval save(StockAdjustmentApproval approval);

    PaginationResult<StockAdjustmentApproval> search(StockAdjustmentApprovalSearchCriteria criteria);

    void deleteById(UUID id);
}
