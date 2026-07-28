package fpt.qn.mes.quality.domain.repository;

import java.util.Optional;
import java.util.UUID;

import fpt.qn.mes.common.dto.response.PaginationResult;
import fpt.qn.mes.quality.domain.entities.QualityInspection;
import fpt.qn.mes.quality.domain.entities.QualityInspectionResult;

public interface QualityInspectionRepository {
    Optional<QualityInspection> findById(UUID id);
    QualityInspection save(QualityInspection inspection);
    void deleteById(UUID id);
    PaginationResult<QualityInspection> findAll(int page, int size);

    QualityInspectionResult saveResult(QualityInspectionResult result);
    PaginationResult<QualityInspectionResult> findResultsByInspectionId(UUID inspectionId, int page, int size);

    /**
     * Compute the sum of all result quantities for a given inspection.
     */
    java.math.BigDecimal sumResultQuantities(UUID inspectionId);

    /**
     * Update the inspection's qc_status_id.
     */
    void updateStatus(UUID inspectionId, UUID newStatusId);

    /**
     * Find the number of results for an inspection.
     */
    int countResults(UUID inspectionId);
}