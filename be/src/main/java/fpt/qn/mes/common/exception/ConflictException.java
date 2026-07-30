package fpt.qn.mes.common.exception;

public class ConflictException extends AppException {

    public ConflictException(String message) {
        super(409, ErrorCode.CONFLICT, message);
    }
}
