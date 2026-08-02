package fpt.qn.mes.report.application.dto.productionoutput.search;

import java.time.LocalDate;
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
public class ProductionOutputSearchRequest extends BaseSearchRequest {
    LocalDate fromDate;
    LocalDate toDate;
}
