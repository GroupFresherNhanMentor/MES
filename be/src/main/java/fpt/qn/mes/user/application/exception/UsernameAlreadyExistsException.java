package fpt.qn.mes.user.application.exception;


import fpt.qn.mes.common.exception.AppException;
import fpt.qn.mes.common.exception.ErrorCode;

public class UsernameAlreadyExistsException extends AppException {

    public UsernameAlreadyExistsException(String message) {
        super(409, ErrorCode.CONFLICT, message);
    }
}
