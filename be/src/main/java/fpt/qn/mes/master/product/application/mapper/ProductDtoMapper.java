package fpt.qn.mes.master.product.application.mapper;

import org.mapstruct.Mapper;

import fpt.qn.mes.master.product.application.dto.product.ProductResponse;
import fpt.qn.mes.master.product.domain.entities.Product;

@Mapper(componentModel = "spring", uses = {ProductTypeDtoMapper.class, UnitOfMeasureDtoMapper.class, ProductStatusDtoMapper.class})
public interface ProductDtoMapper {
    ProductResponse toDto(Product product);
}
