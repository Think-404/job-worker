package org.originit.error.exception;

import lombok.Getter;
import org.originit.error.IErrorCode;

public class DeliverySystemException extends RuntimeException {

    @Getter
    private final IErrorCode errorCode;

    public DeliverySystemException(IErrorCode errorCode) {
        this.errorCode = errorCode;
    }

    public DeliverySystemException(String message, IErrorCode errorCode) {
        super(message);
        this.errorCode = errorCode;
    }

    public DeliverySystemException(String message, Throwable cause, IErrorCode errorCode) {
        super(message, cause);
        this.errorCode = errorCode;
    }

    public DeliverySystemException(Throwable cause, IErrorCode errorCode) {
        super(cause);
        this.errorCode = errorCode;
    }
}
