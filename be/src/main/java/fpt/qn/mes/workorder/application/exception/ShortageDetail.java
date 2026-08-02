package fpt.qn.mes.workorder.application.exception;

import java.math.BigDecimal;
import java.util.UUID;

public record ShortageDetail(
        UUID materialProductId,
        BigDecimal requiredQuantity,
        BigDecimal availableQuantity,
        BigDecimal shortageQuantity) {
}
