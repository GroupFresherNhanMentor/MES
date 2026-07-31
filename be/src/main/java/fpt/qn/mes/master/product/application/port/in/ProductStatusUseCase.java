package fpt.qn.mes.master.product.application.port.in;

import fpt.qn.mes.common.dto.response.PageResponse;
import fpt.qn.mes.master.product.application.dto.productstatus.ProductStatusResponse;
import fpt.qn.mes.master.product.application.dto.productstatus.create.CreateProductStatusRequest;
import fpt.qn.mes.master.product.application.dto.productstatus.search.ProductStatusSearchRequest;

public interface ProductStatusUseCase {
    PageResponse<ProductStatusResponse> getProductStatuses(ProductStatusSearchRequest request);
    void createProductStatus(CreateProductStatusRequest request);
}
