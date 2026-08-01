package fpt.qn.mes.common.exception;

import org.springframework.http.HttpStatus;

public abstract class AppException extends RuntimeException {

    private final HttpStatus status;
    private final ErrorCode errorCode;
    private final Object details;

    public AppException(int status, ErrorCode errorCode, String message) {
        this(status, errorCode, message, null);
    }

    public AppException(int status, ErrorCode errorCode, String message, Object details) {
        super(message);
        this.status = HttpStatus.valueOf(status);
        this.errorCode = errorCode;
        this.details = details;
    }

    public HttpStatus getStatus() {
        return status;
    }

    public ErrorCode getErrorCode() {
        return errorCode;
    }

    public Object getDetails() {
        return details;
    }
}
