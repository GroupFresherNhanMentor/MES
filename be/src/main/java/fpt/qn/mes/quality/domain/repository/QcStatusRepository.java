package fpt.qn.mes.quality.domain.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import fpt.qn.mes.common.domainQuery.PaginationResult;
import fpt.qn.mes.quality.domain.entities.QcStatus;
import fpt.qn.mes.quality.domain.repository.criteria.QcStatusSearchCriteria;

public interface QcStatusRepository {
    Optional<QcStatus> findById(UUID id);
    List<QcStatus> findAll();
    QcStatus save(QcStatus status);
    PaginationResult<QcStatus> search(QcStatusSearchCriteria criteria);
}
