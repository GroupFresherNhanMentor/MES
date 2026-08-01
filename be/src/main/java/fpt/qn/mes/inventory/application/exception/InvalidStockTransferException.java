package fpt.qn.mes.inventory.application.exception;

import org.springframework.http.HttpStatus;

import fpt.qn.mes.common.exception.AppException;
import fpt.qn.mes.common.exception.ErrorCode;

public class InvalidStockTransferException extends AppException {
    public InvalidStockTransferException(String message) {
        super(400, ErrorCode.INVALID_INPUT, message);
    }
}
