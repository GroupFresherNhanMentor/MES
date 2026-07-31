package fpt.qn.mes.master.product.application.exception;

import fpt.qn.mes.common.exception.AppException;
import fpt.qn.mes.common.exception.ErrorCode;

public class ProductTypeNotFoundException extends AppException {
    public ProductTypeNotFoundException(String message) {
        super(404, ErrorCode.NOT_FOUND, message);
    }
}
