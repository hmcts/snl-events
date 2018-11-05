package uk.gov.hmcts.reform.sandl.snlevents.exceptions;

public class SnlRuntimeException extends RuntimeException {
    public SnlRuntimeException() {
        super();
    }

    public SnlRuntimeException(String message) {
        super(message);
    }

    public SnlRuntimeException(String message, Throwable cause) {
        super(message, cause);
    }

    public SnlRuntimeException(Throwable cause) {
        super(cause);
    }

    protected SnlRuntimeException(String message, Throwable cause, boolean enableSuppression,
                                  boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
