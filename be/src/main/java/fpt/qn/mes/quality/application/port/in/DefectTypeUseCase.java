package fpt.qn.mes.quality.application.port.in;

import fpt.qn.mes.common.dto.response.PageResponse;
import fpt.qn.mes.quality.application.dto.defecttype.DefectTypeResponse;
import fpt.qn.mes.quality.application.dto.defecttype.create.CreateDefectTypeRequest;
import fpt.qn.mes.quality.application.dto.defecttype.search.DefectTypeSearchRequest;

public interface DefectTypeUseCase {
    PageResponse<DefectTypeResponse> getDefectTypes(DefectTypeSearchRequest request);
    void createDefectType(CreateDefectTypeRequest request);
}
