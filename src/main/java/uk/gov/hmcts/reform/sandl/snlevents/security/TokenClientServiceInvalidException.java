package uk.gov.hmcts.reform.sandl.snlevents.security;

public class TokenClientServiceInvalidException extends RuntimeException {
    public TokenClientServiceInvalidException() {
        super();
    }

    public TokenClientServiceInvalidException(String message) {
        super(message);
    }

    public TokenClientServiceInvalidException(String message, Throwable cause) {
        super(message, cause);
    }

    public TokenClientServiceInvalidException(Throwable cause) {
        super(cause);
    }

    protected TokenClientServiceInvalidException(String message, Throwable cause, boolean enableSuppression,
                                                 boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
