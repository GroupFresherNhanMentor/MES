package fpt.qn.mes.master.product.application.port.in;

import java.util.UUID;

import fpt.qn.mes.common.dto.response.PageResponse;
import fpt.qn.mes.master.product.application.dto.request.CreateProductRequest;
import fpt.qn.mes.master.product.application.dto.response.ProductDto;
import fpt.qn.mes.master.product.application.dto.request.UpdateProductRequest;

public interface ProductUseCase {
    PageResponse<ProductDto> getProducts(int page, int size);
    ProductDto getProductById(UUID id);
    ProductDto createProduct(CreateProductRequest request, UUID currentUserId);
    ProductDto updateProduct(UUID id, UpdateProductRequest request, UUID currentUserId);
    void deleteProduct(UUID id);
}
