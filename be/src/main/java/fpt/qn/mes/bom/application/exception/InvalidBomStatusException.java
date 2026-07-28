package fpt.qn.mes.bom.application.exception;

import fpt.qn.mes.common.exception.AppException;
import fpt.qn.mes.common.exception.ErrorCode;

public class InvalidBomStatusException extends AppException {

    public InvalidBomStatusException(String message) {
        super(400, ErrorCode.INVALID_INPUT, message);
    }
}
