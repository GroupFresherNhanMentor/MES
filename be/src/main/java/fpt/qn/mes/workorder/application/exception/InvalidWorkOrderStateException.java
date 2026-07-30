package fpt.qn.mes.workorder.application.exception;

import fpt.qn.mes.common.exception.AppException;
import fpt.qn.mes.common.exception.ErrorCode;

public class InvalidWorkOrderStateException extends AppException {
    public InvalidWorkOrderStateException(String message) {
        super(400, ErrorCode.BAD_REQUEST, message);
    }
}
