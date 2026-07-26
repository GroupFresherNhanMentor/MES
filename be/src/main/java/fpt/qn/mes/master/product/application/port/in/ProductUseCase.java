package fpt.qn.mes.master.product.application.port.in;

import java.util.UUID;

import fpt.qn.mes.common.dto.PageResponse;
import fpt.qn.mes.master.product.application.dto.CreateProductRequest;
import fpt.qn.mes.master.product.application.dto.ProductDto;
import fpt.qn.mes.master.product.application.dto.UpdateProductRequest;

public interface ProductUseCase {
    PageResponse<ProductDto> getProducts(int page, int size);
    ProductDto getProductById(UUID id);
    ProductDto createProduct(CreateProductRequest request, UUID currentUserId);
    ProductDto updateProduct(UUID id, UpdateProductRequest request, UUID currentUserId);
    void deleteProduct(UUID id);
}
