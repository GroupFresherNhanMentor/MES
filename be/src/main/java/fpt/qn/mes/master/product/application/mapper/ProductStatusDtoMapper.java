package fpt.qn.mes.master.product.application.mapper;

import org.mapstruct.Mapper;

import fpt.qn.mes.master.product.application.dto.productstatus.ProductStatusResponse;
import fpt.qn.mes.master.product.domain.entities.ProductStatus;

@Mapper(componentModel = "spring")
public interface ProductStatusDtoMapper {
    ProductStatusResponse toDto(ProductStatus status);
}
