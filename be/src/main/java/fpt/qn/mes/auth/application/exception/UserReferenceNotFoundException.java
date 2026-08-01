package fpt.qn.mes.auth.application.exception;

import fpt.qn.mes.common.exception.AppException;
import fpt.qn.mes.common.exception.ErrorCode;

public class UserReferenceNotFoundException extends AppException {

    public UserReferenceNotFoundException(String message) {
        super(404, ErrorCode.NOT_FOUND, message);
    }
}
