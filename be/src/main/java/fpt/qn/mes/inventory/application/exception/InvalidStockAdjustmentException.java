package fpt.qn.mes.inventory.application.exception;

import fpt.qn.mes.common.exception.AppException;
import fpt.qn.mes.common.exception.ErrorCode;

public class InvalidStockAdjustmentException extends AppException {
    public InvalidStockAdjustmentException(String message) {
        super(400, ErrorCode.BAD_REQUEST, message);
    }
}
