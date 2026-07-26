package fpt.qn.mes.maintenance.application.exception;

import org.springframework.http.HttpStatus;
import fpt.qn.mes.common.exception.AppException;
import fpt.qn.mes.common.exception.ErrorCode;

public class MaintenanceTicketNotFoundException extends AppException {
    public MaintenanceTicketNotFoundException(String message) { super(HttpStatus.NOT_FOUND, ErrorCode.NOT_FOUND, message); }
}
