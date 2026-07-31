package fpt.qn.mes.master.product.application.port.in;

import java.util.UUID;

import fpt.qn.mes.common.dto.response.PageResponse;
import fpt.qn.mes.master.product.application.dto.product.ProductResponse;
import fpt.qn.mes.master.product.application.dto.product.create.CreateProductRequest;
import fpt.qn.mes.master.product.application.dto.product.search.ProductSearchRequest;
import fpt.qn.mes.master.product.application.dto.product.update.UpdateProductRequest;

public interface ProductUseCase {
    PageResponse<ProductResponse> getProducts(ProductSearchRequest request);
    ProductResponse getProductById(UUID id);
    void createProduct(CreateProductRequest request);
    void updateProduct(UUID id, UpdateProductRequest request);
    void activateProduct(UUID id);
    void deactivateProduct(UUID id);
}
