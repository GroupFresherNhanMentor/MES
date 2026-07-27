package fpt.qn.mes.master.product.application.exception;

import fpt.qn.mes.common.exception.AppException;
import fpt.qn.mes.common.exception.ErrorCode;

public class ProductNotFoundException extends AppException {
    public ProductNotFoundException(String message) { super(404, ErrorCode.NOT_FOUND, message); }
}
