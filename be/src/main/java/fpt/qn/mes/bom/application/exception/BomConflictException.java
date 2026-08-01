package fpt.qn.mes.bom.application.exception;

import fpt.qn.mes.common.exception.AppException;
import fpt.qn.mes.common.exception.ErrorCode;

public class BomConflictException extends AppException {
    public BomConflictException(String message) {
        super(409, ErrorCode.CONFLICT, message);
    }
}
