package fpt.qn.mes.inventory.domain.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import fpt.qn.mes.common.repository.BaseDomainRepository;
import fpt.qn.mes.inventory.domain.entities.StockAdjustmentApproval;
import fpt.qn.mes.inventory.domain.repository.criteria.StockAdjustmentApprovalSearchCriteria;

public interface StockAdjustmentApprovalRepository extends BaseDomainRepository<StockAdjustmentApproval, UUID> {

    StockAdjustmentApproval save(StockAdjustmentApproval approval);

    List<StockAdjustmentApproval> search(StockAdjustmentApprovalSearchCriteria criteria);

    long count(StockAdjustmentApprovalSearchCriteria criteria);

    void deleteById(UUID id);
}
