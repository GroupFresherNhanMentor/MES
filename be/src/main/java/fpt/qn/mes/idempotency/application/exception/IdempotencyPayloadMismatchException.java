package fpt.qn.mes.idempotency.application.exception;

import fpt.qn.mes.common.exception.AppException;
import fpt.qn.mes.common.exception.ErrorCode;

public class IdempotencyPayloadMismatchException extends AppException {

    public IdempotencyPayloadMismatchException(String message) {
        super(400, ErrorCode.BAD_REQUEST, message);
    }
}
