package fpt.qn.mes.workorder.application.exception;

import fpt.qn.mes.common.exception.AppException;
import fpt.qn.mes.common.exception.ErrorCode;

public class BomNotActiveException extends AppException {
    public BomNotActiveException(String message) {
        super(400, ErrorCode.BOM_NOT_ACTIVE, message);
    }
}
