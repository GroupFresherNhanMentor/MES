package fpt.qn.mes.inventory.application.exception;

import fpt.qn.mes.common.exception.AppException;
import fpt.qn.mes.common.exception.ErrorCode;

public class InsufficientStockException extends AppException {
    public InsufficientStockException(String message) {
        super(400, ErrorCode.INVALID_INPUT, message);
    }
}
