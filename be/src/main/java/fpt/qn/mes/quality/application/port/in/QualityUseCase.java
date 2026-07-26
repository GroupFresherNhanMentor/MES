package fpt.qn.mes.quality.application.port.in;

import java.util.UUID;

import fpt.qn.mes.common.dto.PageResponse;
import fpt.qn.mes.quality.application.dto.*;

public interface QualityUseCase {
    PageResponse<QualityInspectionDto> getInspections(int page, int size);
    QualityInspectionDto getInspectionById(UUID id);
    QualityInspectionDto createInspection(CreateQualityInspectionRequest request);
    void deleteInspection(UUID id);
    PageResponse<QualityInspectionResultDto> getResults(UUID inspectionId, int page, int size);
    QualityInspectionResultDto addResult(UUID inspectionId, CreateInspectionResultRequest request);
}
