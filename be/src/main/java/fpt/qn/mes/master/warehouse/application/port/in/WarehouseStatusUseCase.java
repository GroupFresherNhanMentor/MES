package fpt.qn.mes.master.warehouse.application.port.in;

import fpt.qn.mes.common.dto.response.PageResponse;
import fpt.qn.mes.master.warehouse.application.dto.warehousestatus.WarehouseStatusResponse;
import fpt.qn.mes.master.warehouse.application.dto.warehousestatus.create.CreateWarehouseStatusRequest;
import fpt.qn.mes.master.warehouse.application.dto.warehousestatus.search.WarehouseStatusSearchRequest;

public interface WarehouseStatusUseCase {
    PageResponse<WarehouseStatusResponse> getWarehouseStatuses(WarehouseStatusSearchRequest request);
    void createWarehouseStatus(CreateWarehouseStatusRequest request);
}
