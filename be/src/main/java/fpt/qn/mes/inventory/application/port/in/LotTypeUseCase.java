package fpt.qn.mes.inventory.application.port.in;

import java.util.UUID;

import fpt.qn.mes.common.dto.response.PageResponse;
import fpt.qn.mes.inventory.application.dto.lottype.LotTypeResponse;
import fpt.qn.mes.inventory.application.dto.lottype.create.CreateLotTypeRequest;
import fpt.qn.mes.inventory.application.dto.lottype.search.LotTypeSearchRequest;

public interface LotTypeUseCase {
    PageResponse<LotTypeResponse> getLotTypes(LotTypeSearchRequest request);
    void createLotType(CreateLotTypeRequest request);
    UUID getLotTypeIdByName(String name);
}
