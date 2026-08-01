package fpt.qn.mes.bom.application.exception;

import fpt.qn.mes.common.exception.AppException;
import fpt.qn.mes.common.exception.ErrorCode;

public class DuplicateBomItemException extends AppException {

    public DuplicateBomItemException(String message) {
        super(400, ErrorCode.INVALID_INPUT, message);
    }
}
