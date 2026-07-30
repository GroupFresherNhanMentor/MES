package fpt.qn.mes.quality.application.dto.inspection.search;

import java.util.UUID;

import fpt.qn.mes.common.dto.request.BaseSearchRequest;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class QualityInspectionSearchRequest extends BaseSearchRequest {
    String productCode;
    String productName;
    UUID   productTypeId;
    String workOrderCode;
    String lotNumber;
    String lotType;
    UUID   qcStatusId;
}
