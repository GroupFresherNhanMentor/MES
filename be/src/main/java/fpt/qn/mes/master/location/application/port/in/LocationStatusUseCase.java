package fpt.qn.mes.master.location.application.port.in;

import fpt.qn.mes.common.dto.response.PageResponse;
import fpt.qn.mes.master.location.application.dto.locationstatus.LocationStatusResponse;
import fpt.qn.mes.master.location.application.dto.locationstatus.create.CreateLocationStatusRequest;
import fpt.qn.mes.master.location.application.dto.locationstatus.search.LocationStatusSearchRequest;

public interface LocationStatusUseCase {
    PageResponse<LocationStatusResponse> getLocationStatuses(LocationStatusSearchRequest request);
    void createLocationStatus(CreateLocationStatusRequest request);
}
