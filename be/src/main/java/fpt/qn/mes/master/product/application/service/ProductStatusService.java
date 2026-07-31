package fpt.qn.mes.master.product.application.service;

import java.time.Instant;
import java.util.UUID;

import org.springframework.stereotype.Service;

import fpt.qn.mes.common.util.UuidV7;
import org.springframework.transaction.annotation.Transactional;

import fpt.qn.mes.auth.application.port.out.CurrentUserPort;
import fpt.qn.mes.common.dto.response.PageResponse;
import fpt.qn.mes.master.product.application.dto.productstatus.ProductStatusResponse;
import fpt.qn.mes.master.product.application.dto.productstatus.create.CreateProductStatusRequest;
import fpt.qn.mes.master.product.application.dto.productstatus.search.ProductStatusSearchRequest;
import fpt.qn.mes.master.product.application.mapper.ProductStatusDtoMapper;
import fpt.qn.mes.master.product.application.exception.ProductConflictException;
import fpt.qn.mes.master.product.application.port.in.ProductStatusUseCase;
import fpt.qn.mes.master.product.domain.entities.ProductStatus;
import fpt.qn.mes.master.product.domain.repository.ProductStatusRepository;
import fpt.qn.mes.master.product.domain.repository.criteria.ProductStatusSearchCriteria;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ProductStatusService implements ProductStatusUseCase {

    ProductStatusRepository productStatusRepository;
    ProductStatusDtoMapper mapper;
    CurrentUserPort currentUserPort;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<ProductStatusResponse> getProductStatuses(ProductStatusSearchRequest request) {
        ProductStatusSearchCriteria criteria = ProductStatusSearchCriteria.builder()
            .page(request.getPage())
            .size(request.getSize())
            .sort(request.getSort())
            .name(request.getName())
            .build();
        var result = productStatusRepository.search(criteria);
        return PageResponse.of(
            result.getItems().stream().map(s -> mapper.toDto(s)).toList(),
            result.getTotal(), criteria.getPage(), criteria.getSize());
    }

    @Override
    @Transactional
    public void createProductStatus(CreateProductStatusRequest request) {
        if (productStatusRepository.existsByName(request.getName())) {
            throw new ProductConflictException("Product status name already exists: " + request.getName());
        }
        UUID currentUserId = currentUserPort.getCurrentUserId();
        ProductStatus.UserRef userRef = ProductStatus.UserRef.builder().id(currentUserId).build();
        Instant now = Instant.now();
        ProductStatus status = ProductStatus.builder()
            .id(UuidV7.generate())
            .name(request.getName())
            .description(request.getDescription())
            .createdBy(userRef)
            .updatedBy(userRef)
            .createdAt(now)
            .updatedAt(now)
            .build();
        productStatusRepository.save(status);
    }
}
