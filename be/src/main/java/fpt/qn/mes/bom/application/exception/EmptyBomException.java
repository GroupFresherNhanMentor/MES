package fpt.qn.mes.bom.application.exception;

import fpt.qn.mes.common.exception.AppException;
import fpt.qn.mes.common.exception.ErrorCode;

public class EmptyBomException extends AppException {

    public EmptyBomException(String message) {
        super(400, ErrorCode.INVALID_INPUT, message);
    }
}
