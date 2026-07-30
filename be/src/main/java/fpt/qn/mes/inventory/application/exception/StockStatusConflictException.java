package fpt.qn.mes.inventory.application.exception;

import fpt.qn.mes.common.exception.AppException;
import fpt.qn.mes.common.exception.ErrorCode;

public class StockStatusConflictException extends AppException {
    public StockStatusConflictException(String message) {
        super(409, ErrorCode.CONFLICT, message);
    }
}
