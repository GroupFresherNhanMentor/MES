package fpt.qn.mes.inventory.application.exception;

import fpt.qn.mes.common.exception.AppException;
import fpt.qn.mes.common.exception.ErrorCode;

public class StockLotNotFoundException extends AppException {
    public StockLotNotFoundException(String message) { super(404, ErrorCode.NOT_FOUND, message); }
}
