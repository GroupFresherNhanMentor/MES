package fpt.qn.mes.master.product.application.service;

import java.util.UUID;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import fpt.qn.mes.audit.domain.entities.AuditAction;
import fpt.qn.mes.audit.domain.events.AuditEvent;
import fpt.qn.mes.auth.application.port.out.CurrentUserPort;
import fpt.qn.mes.common.port.out.JsonSerializerPort;
import fpt.qn.mes.common.dto.response.PageResponse;
import fpt.qn.mes.common.util.PaginationUtils;
import fpt.qn.mes.master.product.application.dto.product.ProductResponse;
import fpt.qn.mes.master.product.application.dto.product.create.CreateProductRequest;
import fpt.qn.mes.master.product.application.dto.product.search.ProductSearchRequest;
import fpt.qn.mes.master.product.application.dto.product.update.UpdateProductRequest;
import fpt.qn.mes.master.product.application.exception.ProductConflictException;
import fpt.qn.mes.master.product.application.exception.ProductNotFoundException;
import fpt.qn.mes.master.product.application.exception.ProductStatusNotFoundException;
import fpt.qn.mes.master.product.application.exception.ProductTypeNotFoundException;
import fpt.qn.mes.master.product.application.exception.UnitOfMeasureNotFoundException;
import fpt.qn.mes.master.product.application.mapper.ProductDtoMapper;
import fpt.qn.mes.master.product.application.port.in.ProductUseCase;
import fpt.qn.mes.master.product.application.port.out.MovementStockCheckPort;
import fpt.qn.mes.master.product.domain.constants.ProductStatusConstants;
import fpt.qn.mes.master.product.domain.entities.Product;
import fpt.qn.mes.master.product.domain.entities.ProductStatus;
import fpt.qn.mes.master.product.domain.repository.ProductRepository;
import fpt.qn.mes.master.product.domain.repository.ProductStatusRepository;
import fpt.qn.mes.master.product.domain.repository.ProductTypeRepository;
import fpt.qn.mes.master.product.domain.repository.UnitOfMeasureRepository;
import fpt.qn.mes.master.product.domain.repository.criteria.ProductSearchCriteria;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ProductService implements ProductUseCase {

    ProductRepository productRepository;
    ProductStatusRepository productStatusRepository;
    ProductTypeRepository productTypeRepository;
    UnitOfMeasureRepository unitOfMeasureRepository;
    MovementStockCheckPort movementStockCheckPort;
    ProductDtoMapper mapper;
    CurrentUserPort currentUserPort;
    ApplicationEventPublisher eventPublisher;
    JsonSerializerPort jsonSerializer;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<ProductResponse> getProducts(ProductSearchRequest request) {
        var criteria = ProductSearchCriteria.builder()
                .code(request.getCode())
                .name(request.getName())
                .version(request.getVersion())
                .productTypeId(request.getProductTypeId())
                .unitId(request.getUnitId())
                .productStatusId(request.getProductStatusId())
                .page(request.getPage())
                .size(request.getSize())
                .sort(request.getSort())
                .build();
        var result = productRepository.search(criteria);
        return PageResponse.<ProductResponse>builder()
                .items(result.getItems().stream().map(p -> mapper.toDto(p)).toList())
                .totalElements(result.getTotal())
                .pageNumber(request.getPage())
                .pageSize(request.getSize())
                .totalPages(PaginationUtils.calculateTotalPages(result.getTotal(), request.getSize()))
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public ProductResponse getProductById(UUID id) {
        return productRepository.findById(id)
                .map(p -> mapper.toDto(p))
                .orElseThrow(() -> new ProductNotFoundException("Product not found: " + id));
    }

    @Override
    @Transactional
    public void createProduct(CreateProductRequest request) {
        if (productRepository.existsByCode(request.getCode())) {
            throw new ProductConflictException("Product code already exists: " + request.getCode());
        }
        if (productRepository.existsByVersion(request.getVersion())) {
            throw new ProductConflictException("Product version already exists: " + request.getVersion());
        }
        if (!productTypeRepository.existsById(request.getProductTypeId())) {
            throw new ProductTypeNotFoundException("Product type not found: " + request.getProductTypeId());
        }
        if (!unitOfMeasureRepository.existsById(request.getUnitId())) {
            throw new UnitOfMeasureNotFoundException("Unit of measure not found: " + request.getUnitId());
        }
        ProductStatus activeStatus = productStatusRepository.findByName(ProductStatusConstants.ACTIVE)
                .orElseThrow(() -> new ProductStatusNotFoundException("ACTIVE status not found"));
        UUID currentUserId = currentUserPort.getCurrentUserId();
        var product = Product.create(request.getCode(), request.getName(), request.getVersion(), request.getProductTypeId(), request.getUnitId(), activeStatus.getId(), currentUserId);
        productRepository.save(product);
        eventPublisher.publishEvent(AuditEvent.create(currentUserId, AuditAction.CREATE_PRODUCT,
                "PRODUCT", product.getId(), null, jsonSerializer.toJson(product), null));
    }

    @Override
    @Transactional
    public void updateProduct(UUID id, UpdateProductRequest request) {
        var existing = productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException("Product not found: " + id));
        boolean hasMovements = movementStockCheckPort.hasStockMovements(id);
        if (hasMovements && request.getProductTypeId() != null && !request.getProductTypeId().equals(existing.getProductType().getId())) {
            throw new ProductConflictException("Cannot change product type — product already has stock movements: " + id);
        }
        if (hasMovements && request.getUnitId() != null && !request.getUnitId().equals(existing.getUnit().getId())) {
            throw new ProductConflictException("Cannot change unit of measure — product already has stock movements: " + id);
        }
        if (request.getProductTypeId() != null && !productTypeRepository.existsById(request.getProductTypeId())) {
            throw new ProductTypeNotFoundException("Product type not found: " + request.getProductTypeId());
        }
        if (request.getUnitId() != null && !unitOfMeasureRepository.existsById(request.getUnitId())) {
            throw new UnitOfMeasureNotFoundException("Unit of measure not found: " + request.getUnitId());
        }
        UUID currentUserId = currentUserPort.getCurrentUserId();
        var updated = Product.update(existing, request.getName(), request.getProductTypeId(), request.getUnitId(), currentUserId);
        productRepository.update(updated);
        eventPublisher.publishEvent(AuditEvent.create(currentUserId, AuditAction.UPDATE_PRODUCT,
                "PRODUCT", id, jsonSerializer.toJson(existing), jsonSerializer.toJson(updated), null));
    }

    @Override
    @Transactional
    public void activateProduct(UUID id) {
        var existing = productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException("Product not found: " + id));
        ProductStatus activeStatus = productStatusRepository.findByName(ProductStatusConstants.ACTIVE)
                .orElseThrow(() -> new ProductStatusNotFoundException("ACTIVE status not found"));
        UUID currentUserId = currentUserPort.getCurrentUserId();
        var activated = Product.changeStatus(existing, activeStatus.getId(), currentUserId);
        productRepository.update(activated);
        eventPublisher.publishEvent(AuditEvent.create(currentUserId, AuditAction.ACTIVATE_PRODUCT,
                "PRODUCT", id, jsonSerializer.toJson(existing), jsonSerializer.toJson(activated), null));
    }

    @Override
    @Transactional
    public void deactivateProduct(UUID id) {
        var existing = productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException("Product not found: " + id));
        ProductStatus inactiveStatus = productStatusRepository.findByName(ProductStatusConstants.INACTIVE)
                .orElseThrow(() -> new ProductStatusNotFoundException("INACTIVE status not found"));
        UUID currentUserId = currentUserPort.getCurrentUserId();
        var deactivated = Product.changeStatus(existing, inactiveStatus.getId(), currentUserId);
        productRepository.update(deactivated);
        eventPublisher.publishEvent(AuditEvent.create(currentUserId, AuditAction.DEACTIVATE_PRODUCT,
                "PRODUCT", id, jsonSerializer.toJson(existing), jsonSerializer.toJson(deactivated), null));
    }
}
