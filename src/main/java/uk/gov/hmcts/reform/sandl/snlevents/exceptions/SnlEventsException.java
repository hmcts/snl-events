package uk.gov.hmcts.reform.sandl.snlevents.exceptions;

public class SnlEventsException extends RuntimeException {
    public SnlEventsException() {
        super();
    }

    public SnlEventsException(String message) {
        super(message);
    }

    public SnlEventsException(String message, Throwable cause) {
        super(message, cause);
    }

    public SnlEventsException(Throwable cause) {
        super(cause);
    }

    protected SnlEventsException(String message, Throwable cause, boolean enableSuppression,
                                 boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
