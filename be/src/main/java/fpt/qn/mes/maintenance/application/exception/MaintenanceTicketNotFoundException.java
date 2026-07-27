package fpt.qn.mes.maintenance.application.exception;

import fpt.qn.mes.common.exception.AppException;
import fpt.qn.mes.common.exception.ErrorCode;

public class MaintenanceTicketNotFoundException extends AppException {
    public MaintenanceTicketNotFoundException(String message) { super(404, ErrorCode.NOT_FOUND, message); }
}
