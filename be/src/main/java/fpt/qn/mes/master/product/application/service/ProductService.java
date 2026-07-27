package fpt.qn.mes.master.product.application.service;

import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import fpt.qn.mes.common.dto.response.PageResponse;
import fpt.qn.mes.master.product.application.dto.request.CreateProductRequest;
import fpt.qn.mes.master.product.application.dto.response.ProductDto;
import fpt.qn.mes.master.product.application.dto.request.UpdateProductRequest;
import fpt.qn.mes.master.product.application.mapper.ProductDtoMapper;
import fpt.qn.mes.master.product.application.port.in.ProductUseCase;
import fpt.qn.mes.master.product.domain.repository.ProductRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ProductService implements ProductUseCase {

    ProductRepository productRepository;
    ProductDtoMapper mapper;

    @Override @Transactional(readOnly = true)
    public PageResponse<ProductDto> getProducts(int page, int size) {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override @Transactional(readOnly = true)
    public ProductDto getProductById(UUID id) {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override @Transactional
    public ProductDto createProduct(CreateProductRequest request, UUID currentUserId) {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override @Transactional
    public ProductDto updateProduct(UUID id, UpdateProductRequest request, UUID currentUserId) {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override @Transactional
    public void deleteProduct(UUID id) {}
}
