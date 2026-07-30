package fpt.qn.mes.master.location.application.exception;

import fpt.qn.mes.common.exception.AppException;
import fpt.qn.mes.common.exception.ErrorCode;

public class LocationStatusNotFoundException extends AppException {
    public LocationStatusNotFoundException(String message) {
        super(404, ErrorCode.NOT_FOUND, message);
    }
}
