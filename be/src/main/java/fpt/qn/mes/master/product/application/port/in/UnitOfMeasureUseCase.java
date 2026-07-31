package fpt.qn.mes.master.product.application.port.in;

import fpt.qn.mes.common.dto.response.PageResponse;
import fpt.qn.mes.master.product.application.dto.unitofmeasure.UnitOfMeasureResponse;
import fpt.qn.mes.master.product.application.dto.unitofmeasure.create.CreateUnitOfMeasureRequest;
import fpt.qn.mes.master.product.application.dto.unitofmeasure.search.UnitOfMeasureSearchRequest;

public interface UnitOfMeasureUseCase {
    PageResponse<UnitOfMeasureResponse> getUnitsOfMeasure(UnitOfMeasureSearchRequest request);
    void createUnitOfMeasure(CreateUnitOfMeasureRequest request);
}
