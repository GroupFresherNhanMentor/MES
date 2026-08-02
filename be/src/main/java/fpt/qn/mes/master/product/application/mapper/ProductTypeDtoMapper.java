package fpt.qn.mes.master.product.application.mapper;

import org.mapstruct.Mapper;

import fpt.qn.mes.master.product.application.dto.producttype.ProductTypeResponse;
import fpt.qn.mes.master.product.domain.entities.ProductType;

@Mapper(componentModel = "spring")
public interface ProductTypeDtoMapper {
    ProductTypeResponse toDto(ProductType type);
}
