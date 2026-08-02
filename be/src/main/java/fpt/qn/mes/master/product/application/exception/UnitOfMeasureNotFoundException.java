package fpt.qn.mes.master.product.application.exception;

import fpt.qn.mes.common.exception.AppException;
import fpt.qn.mes.common.exception.ErrorCode;

public class UnitOfMeasureNotFoundException extends AppException {
    public UnitOfMeasureNotFoundException(String message) {
        super(404, ErrorCode.NOT_FOUND, message);
    }
}
