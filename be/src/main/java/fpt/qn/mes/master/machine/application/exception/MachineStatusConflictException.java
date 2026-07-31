package fpt.qn.mes.master.machine.application.exception;

import fpt.qn.mes.common.exception.AppException;
import fpt.qn.mes.common.exception.ErrorCode;

public class MachineStatusConflictException extends AppException {
    public MachineStatusConflictException(String message) {
        super(409, ErrorCode.CONFLICT, message);
    }
}

