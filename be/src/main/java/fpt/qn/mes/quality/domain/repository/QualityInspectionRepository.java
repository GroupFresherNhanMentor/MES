package fpt.qn.mes.quality.domain.repository;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import fpt.qn.mes.common.domainQuery.PaginationResult;
import fpt.qn.mes.quality.domain.entities.QualityInspection;
import fpt.qn.mes.quality.domain.entities.QualityInspectionResult;
import fpt.qn.mes.quality.domain.repository.criteria.QualityInspectionResultSearchCriteria;
import fpt.qn.mes.quality.domain.repository.criteria.QualityInspectionSearchCriteria;

public interface QualityInspectionRepository {
    Optional<QualityInspection> findById(UUID id);

    QualityInspection save(QualityInspection inspection);

    PaginationResult<QualityInspection> search(QualityInspectionSearchCriteria criteria);

    QualityInspectionResult saveResult(QualityInspectionResult result);
    PaginationResult<QualityInspectionResult> searchResults(QualityInspectionResultSearchCriteria criteria);

    BigDecimal sumResultQuantities(UUID inspectionId);
    void updateStatus(UUID inspectionId, UUID newStatusId);
}
