package fpt.qn.mes.master.location.application.exception;

import org.springframework.http.HttpStatus;
import fpt.qn.mes.common.exception.AppException;
import fpt.qn.mes.common.exception.ErrorCode;

public class LocationNotFoundException extends AppException {
    public LocationNotFoundException(String message) { super(HttpStatus.NOT_FOUND, ErrorCode.NOT_FOUND, message); }
}
