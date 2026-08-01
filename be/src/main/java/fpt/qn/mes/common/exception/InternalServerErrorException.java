package fpt.qn.mes.common.exception;

public class InternalServerErrorException extends AppException {
    public InternalServerErrorException(String message) {
        super(500, ErrorCode.INTERNAL_SERVER_ERROR, message);
    }
}
