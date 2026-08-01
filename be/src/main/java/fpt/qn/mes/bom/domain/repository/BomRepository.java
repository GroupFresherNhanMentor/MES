package fpt.qn.mes.bom.domain.repository;

import java.util.Optional;
import java.util.UUID;

import fpt.qn.mes.bom.domain.entities.Bom;
import fpt.qn.mes.bom.domain.repository.criteria.BomSearchCriteria;
import fpt.qn.mes.common.domainQuery.PaginationResult;

public interface BomRepository {
    Optional<Bom> findById(UUID id);
    Optional<Bom> findActiveByFinishedProductId(UUID finishedProductId);
    Bom save(Bom bom);
    Bom update(Bom bom);
    PaginationResult<Bom> search(BomSearchCriteria criteria);
    boolean existsByFinishedProductIdAndVersion(UUID finishedProductId, Integer version);
    void deactivateActiveBomsForProduct(UUID finishedProductId, UUID activeStatusId, UUID inactiveStatusId);
    int findMaxVersionByFinishedProductId(UUID finishedProductId);
}
