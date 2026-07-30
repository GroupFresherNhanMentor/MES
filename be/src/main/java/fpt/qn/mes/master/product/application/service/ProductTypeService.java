package fpt.qn.mes.master.product.application.service;

import java.time.Instant;
import java.util.UUID;

import org.springframework.stereotype.Service;

import fpt.qn.mes.common.util.UuidV7;
import org.springframework.transaction.annotation.Transactional;

import fpt.qn.mes.auth.application.port.out.CurrentUserPort;
import fpt.qn.mes.common.dto.response.PageResponse;
import fpt.qn.mes.master.product.application.dto.producttype.ProductTypeResponse;
import fpt.qn.mes.master.product.application.dto.producttype.create.CreateProductTypeRequest;
import fpt.qn.mes.master.product.application.dto.producttype.search.ProductTypeSearchRequest;
import fpt.qn.mes.master.product.application.mapper.ProductTypeDtoMapper;
import fpt.qn.mes.master.product.application.port.in.ProductTypeUseCase;
import fpt.qn.mes.master.product.domain.entities.ProductType;
import fpt.qn.mes.master.product.domain.repository.ProductTypeRepository;
import fpt.qn.mes.master.product.domain.repository.criteria.ProductTypeSearchCriteria;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ProductTypeService implements ProductTypeUseCase {

    ProductTypeRepository productTypeRepository;
    ProductTypeDtoMapper mapper;
    CurrentUserPort currentUserPort;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<ProductTypeResponse> getProductTypes(ProductTypeSearchRequest request) {
        ProductTypeSearchCriteria criteria = ProductTypeSearchCriteria.builder()
            .page(request.getPage())
            .size(request.getSize())
            .sort(request.getSort())
            .name(request.getName())
            .build();
        var result = productTypeRepository.search(criteria);
        return PageResponse.of(
            result.getItems().stream().map(t -> mapper.toDto(t)).toList(),
            result.getTotal(), criteria.getPage(), criteria.getSize());
    }

    @Override
    @Transactional
    public void createProductType(CreateProductTypeRequest request) {
        UUID currentUserId = currentUserPort.getCurrentUserId();
        Instant now = Instant.now();
        ProductType type = ProductType.builder()
            .id(UuidV7.generate())
            .name(request.getName())
            .description(request.getDescription())
            .createdBy(currentUserId)
            .updatedBy(currentUserId)
            .createdAt(now)
            .updatedAt(now)
            .build();
        productTypeRepository.save(type);
    }
}
