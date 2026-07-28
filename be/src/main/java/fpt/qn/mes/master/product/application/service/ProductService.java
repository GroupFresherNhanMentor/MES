package fpt.qn.mes.master.product.application.service;

import java.time.Instant;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import fpt.qn.mes.common.dto.response.PageResponse;
import fpt.qn.mes.master.product.application.dto.request.CreateProductRequest;
import fpt.qn.mes.master.product.application.dto.response.ProductDto;
import fpt.qn.mes.master.product.application.dto.request.UpdateProductRequest;
import fpt.qn.mes.master.product.application.exception.ProductConflictException;
import fpt.qn.mes.master.product.application.exception.ProductNotFoundException;
import fpt.qn.mes.master.product.application.mapper.ProductDtoMapper;
import fpt.qn.mes.master.product.application.port.in.ProductUseCase;
import fpt.qn.mes.master.product.domain.entities.Product;
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

    @Override
    @Transactional(readOnly = true)
    public PageResponse<ProductDto> getProducts(int page, int size) {
        var result = productRepository.findAll(page, size);
        var items = result.getItems().stream()
                .map(mapper::toDto)
                .toList();
        return PageResponse.<ProductDto>builder()
                .items(items)
                .totalElements(result.getTotal())
                .pageNumber(page)
                .pageSize(size)
                .totalPages((int) Math.ceil((double) result.getTotal() / size))
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public ProductDto getProductById(UUID id) {
        return productRepository.findById(id)
                .map(mapper::toDto)
                .orElseThrow(() -> new ProductNotFoundException("Product not found: " + id));
    }

    @Override
    @Transactional
    public ProductDto createProduct(CreateProductRequest request, UUID currentUserId) {
        if (productRepository.existsByCode(request.getCode())) {
            throw new ProductConflictException("Product code already exists: " + request.getCode());
        }
        var product = Product.create(
                request.getCode(),
                request.getName(),
                request.getProductTypeId(),
                request.getUnitId(),
                request.getProductStatusId(),
                currentUserId);
        var saved = productRepository.save(product);
        return mapper.toDto(saved);
    }

    @Override
    @Transactional
    public ProductDto updateProduct(UUID id, UpdateProductRequest request, UUID currentUserId) {
        var existing = productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException("Product not found: " + id));

        var updated = Product.builder()
                .id(existing.getId())
                .code(existing.getCode())
                .name(request.getName() != null ? request.getName() : existing.getName())
                .productTypeId(existing.getProductTypeId())
                .unitId(request.getUnitId() != null ? request.getUnitId() : existing.getUnitId())
                .productStatusId(request.getProductStatusId() != null ? request.getProductStatusId() : existing.getProductStatusId())
                .version(existing.getVersion())
                .createdAt(existing.getCreatedAt())
                .createdBy(existing.getCreatedBy())
                .updatedAt(Instant.now())
                .updatedBy(currentUserId)
                .build();

        var saved = productRepository.update(updated);
        return mapper.toDto(saved);
    }

    @Override
    @Transactional
    public void deleteProduct(UUID id) {
        var existing = productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException("Product not found: " + id));
        var deactivated = Product.builder()
                .id(existing.getId())
                .code(existing.getCode())
                .name(existing.getName())
                .productTypeId(existing.getProductTypeId())
                .unitId(existing.getUnitId())
                .productStatusId(null)
                .version(existing.getVersion())
                .createdAt(existing.getCreatedAt())
                .createdBy(existing.getCreatedBy())
                .updatedAt(Instant.now())
                .updatedBy(existing.getUpdatedBy())
                .build();
        productRepository.update(deactivated);
    }
}
