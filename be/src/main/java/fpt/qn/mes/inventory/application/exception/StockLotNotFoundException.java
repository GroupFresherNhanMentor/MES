package fpt.qn.mes.inventory.application.exception;

import org.springframework.http.HttpStatus;
import fpt.qn.mes.common.exception.AppException;
import fpt.qn.mes.common.exception.ErrorCode;

public class StockLotNotFoundException extends AppException {
    public StockLotNotFoundException(String message) { super(HttpStatus.NOT_FOUND, ErrorCode.NOT_FOUND, message); }
}
