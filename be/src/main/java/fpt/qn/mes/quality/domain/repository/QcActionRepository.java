package fpt.qn.mes.quality.domain.repository;

import java.util.Optional;
import java.util.UUID;

import fpt.qn.mes.common.domainQuery.PaginationResult;
import fpt.qn.mes.quality.domain.entities.QcAction;
import fpt.qn.mes.quality.domain.repository.criteria.QcActionSearchCriteria;

public interface QcActionRepository {
    Optional<QcAction> findById(UUID id);
    QcAction save(QcAction action);
    PaginationResult<QcAction> search(QcActionSearchCriteria criteria);
}
