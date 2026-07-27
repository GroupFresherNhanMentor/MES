package fpt.qn.mes.bom.application.exception;

import fpt.qn.mes.common.exception.AppException;
import fpt.qn.mes.common.exception.ErrorCode;

public class BomNotFoundException extends AppException {
    public BomNotFoundException(String message) { super(404, ErrorCode.NOT_FOUND, message); }
}
