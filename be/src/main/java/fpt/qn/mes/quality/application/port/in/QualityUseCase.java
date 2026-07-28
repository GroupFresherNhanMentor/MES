package fpt.qn.mes.quality.application.port.in;

import java.util.List;
import java.util.UUID;

import fpt.qn.mes.common.dto.response.PageResponse;
import fpt.qn.mes.quality.application.dto.request.*;
import fpt.qn.mes.quality.application.dto.response.*;

public interface QualityUseCase {
    PageResponse<QualityInspectionDto> getInspections(int page, int size);
    QualityInspectionDto getInspectionById(UUID id);
    QualityInspectionDto createInspection(CreateQualityInspectionRequest request);
    void deleteInspection(UUID id);

    PassQcResponse passInspection(UUID inspectionId, PassQcRequest request);
    FailQcResponse failInspection(UUID inspectionId, FailQcRequest request);

    List<QcStatusDto> getQcStatuses();
    QcStatusDto createQcStatus(CreateLookupRequest request);
    List<QcActionDto> getQcActions();
    QcActionDto createQcAction(CreateLookupRequest request);
    List<DefectTypeDto> getDefectTypes();
    DefectTypeDto createDefectType(CreateDefectTypeRequest request);
}