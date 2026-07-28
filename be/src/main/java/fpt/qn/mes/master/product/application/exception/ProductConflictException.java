package fpt.qn.mes.master.product.application.exception;

import fpt.qn.mes.common.exception.AppException;
import fpt.qn.mes.common.exception.ErrorCode;

public class ProductConflictException extends AppException {
    public ProductConflictException(String message) { super(409, ErrorCode.CONFLICT, message); }
}
