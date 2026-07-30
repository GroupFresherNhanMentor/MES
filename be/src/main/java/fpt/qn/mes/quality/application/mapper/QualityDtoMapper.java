package fpt.qn.mes.quality.application.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import fpt.qn.mes.quality.application.dto.inspection.QualityInspectionResponse;
import fpt.qn.mes.quality.application.dto.inspection.QualityInspectionResultResponse;
import fpt.qn.mes.quality.domain.entities.QualityInspection;
import fpt.qn.mes.quality.domain.entities.QualityInspectionResult;

@Mapper(componentModel = "spring")
public interface QualityDtoMapper {

    @Mapping(target = "workOrder.workOrderId",   source = "workOrder.id")
    @Mapping(target = "workOrder.workOrderCode", source = "workOrder.code")
    @Mapping(target = "product.productId",       source = "product.id")
    @Mapping(target = "product.productCode",     source = "product.code")
    @Mapping(target = "product.productName",     source = "product.name")
    @Mapping(target = "lot.lotId",               source = "lot.id")
    @Mapping(target = "lot.lotNumber",           source = "lot.lotNumber")
    @Mapping(target = "lot.lotType",             source = "lot.lotType")
    @Mapping(target = "qcStatusName",            source = "qcStatus.name")
    QualityInspectionResponse toDto(QualityInspection inspection);

    @Mapping(target = "defectTypeName",       source = "defectType.name")
    @Mapping(target = "actionName",           source = "action.name")
    @Mapping(target = "inspector.userId",     source = "inspector.userId")
    @Mapping(target = "inspector.username",   source = "inspector.username")
    @Mapping(target = "inspector.fullName",   source = "inspector.fullName")
    QualityInspectionResultResponse toDto(QualityInspectionResult result);
}
