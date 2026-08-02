package fpt.qn.mes.inventory.application.exception;

import fpt.qn.mes.common.exception.AppException;
import fpt.qn.mes.common.exception.ErrorCode;

public class InvalidStockLotException extends AppException {
    public InvalidStockLotException(String message) {
        super(400, ErrorCode.BAD_REQUEST, message);
    }
}
