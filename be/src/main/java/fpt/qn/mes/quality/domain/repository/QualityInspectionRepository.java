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
}
