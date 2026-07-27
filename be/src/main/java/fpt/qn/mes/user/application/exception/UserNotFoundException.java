package fpt.qn.mes.user.application.exception;


import fpt.qn.mes.common.exception.AppException;
import fpt.qn.mes.common.exception.ErrorCode;

public class UserNotFoundException extends AppException {

    public UserNotFoundException(String message) {
        super(404, ErrorCode.NOT_FOUND, message);
    }
}
