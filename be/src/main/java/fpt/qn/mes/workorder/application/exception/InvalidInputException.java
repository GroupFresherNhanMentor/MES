package fpt.qn.mes.workorder.application.exception;

import fpt.qn.mes.common.exception.AppException;
import fpt.qn.mes.common.exception.ErrorCode;

public class InvalidInputException extends AppException {
    public InvalidInputException(String message) {
        super(400, ErrorCode.INVALID_INPUT, message);
    }
}
