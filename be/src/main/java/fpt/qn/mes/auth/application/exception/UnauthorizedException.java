package fpt.qn.mes.auth.application.exception;


import fpt.qn.mes.common.exception.AppException;
import fpt.qn.mes.common.exception.ErrorCode;

public class UnauthorizedException extends AppException {
    public UnauthorizedException(String message) {
        super(401, ErrorCode.UNAUTHORIZED, message);
    }
}
