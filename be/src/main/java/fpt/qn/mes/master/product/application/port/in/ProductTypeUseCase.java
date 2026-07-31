package fpt.qn.mes.master.product.application.port.in;

import fpt.qn.mes.common.dto.response.PageResponse;
import fpt.qn.mes.master.product.application.dto.producttype.ProductTypeResponse;
import fpt.qn.mes.master.product.application.dto.producttype.create.CreateProductTypeRequest;
import fpt.qn.mes.master.product.application.dto.producttype.search.ProductTypeSearchRequest;

public interface ProductTypeUseCase {
    PageResponse<ProductTypeResponse> getProductTypes(ProductTypeSearchRequest request);
    void createProductType(CreateProductTypeRequest request);
}
