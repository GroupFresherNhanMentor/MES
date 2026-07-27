package fpt.qn.mes.master.product.application.mapper;

import org.mapstruct.Mapper;

import fpt.qn.mes.master.product.application.dto.response.ProductDto;
import fpt.qn.mes.master.product.domain.entities.Product;

@Mapper(componentModel = "spring")
public interface ProductDtoMapper {

    ProductDto toDto(Product product);
}
