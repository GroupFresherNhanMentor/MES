package fpt.qn.mes.report.application.dto.defectrate;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class DefectRateResponse {
    UUID productId;
    String productCode;
    String productName;
    BigDecimal totalInspected;
    BigDecimal defectQuantity;
    BigDecimal scrapQuantity;
    BigDecimal defectRate;
    List<String> topDefectTypes;
}
