package fpt.qn.mes.quality.application.port.in;

import java.util.UUID;

import fpt.qn.mes.common.dto.response.PageResponse;
import fpt.qn.mes.quality.application.dto.inspection.QualityInspectionResponse;
import fpt.qn.mes.quality.application.dto.inspection.QualityInspectionResultResponse;
import fpt.qn.mes.quality.application.dto.inspection.create.CreateQualityInspectionRequest;
import fpt.qn.mes.quality.application.dto.inspection.fail.FailQcRequest;
import fpt.qn.mes.quality.application.dto.inspection.pass.PassQcRequest;
import fpt.qn.mes.quality.application.dto.inspection.result.search.QualityInspectionResultSearchRequest;
import fpt.qn.mes.quality.application.dto.inspection.search.QualityInspectionSearchRequest;

public interface QualityUseCase {
    PageResponse<QualityInspectionResponse> getInspections(QualityInspectionSearchRequest request);
    void createInspection(CreateQualityInspectionRequest request);

    PageResponse<QualityInspectionResultResponse> getInspectionResults(UUID inspectionId, QualityInspectionResultSearchRequest request);

    void passInspection(UUID inspectionId, PassQcRequest request);
    void failInspection(UUID inspectionId, FailQcRequest request);
}
