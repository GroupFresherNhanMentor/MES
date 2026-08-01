package fpt.qn.mes.auth.application.exception;

import fpt.qn.mes.common.exception.AppException;
import fpt.qn.mes.common.exception.ErrorCode;

public class RoleConflictException extends AppException {

    public RoleConflictException(String message) {
        super(409, ErrorCode.CONFLICT, message);
    }
}
