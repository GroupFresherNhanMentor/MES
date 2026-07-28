package fpt.qn.mes.quality.application.exception;

import fpt.qn.mes.common.exception.AppException;
import fpt.qn.mes.common.exception.ErrorCode;

public class InvalidQcActionException extends AppException {
    public InvalidQcActionException(String message) {
        super(400, ErrorCode.INVALID_INPUT, message);
    }
}
