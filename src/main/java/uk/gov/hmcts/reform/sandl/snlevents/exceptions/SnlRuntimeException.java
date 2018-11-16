package uk.gov.hmcts.reform.sandl.snlevents.exceptions;

public class SnlRuntimeException extends RuntimeException {
    public SnlRuntimeException(Throwable cause) {
        super(cause);
    }

    public SnlRuntimeException(String cause) {
        super(cause);
    }
}
