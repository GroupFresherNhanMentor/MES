package fpt.qn.mes.common.idempotency.application.exception;

import fpt.qn.mes.common.exception.AppException;
import fpt.qn.mes.common.exception.ErrorCode;

public class IdempotencyConflictException extends AppException {

    public IdempotencyConflictException(String message) {
        super(409, ErrorCode.CONFLICT, message);
    }
}
