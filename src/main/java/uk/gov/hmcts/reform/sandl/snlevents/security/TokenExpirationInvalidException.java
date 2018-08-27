package uk.gov.hmcts.reform.sandl.snlevents.security;

public class TokenExpirationInvalidException extends RuntimeException {
    public TokenExpirationInvalidException() {
        super();
    }

    public TokenExpirationInvalidException(String message) {
        super(message);
    }

    public TokenExpirationInvalidException(String message, Throwable cause) {
        super(message, cause);
    }

    public TokenExpirationInvalidException(Throwable cause) {
        super(cause);
    }

    protected TokenExpirationInvalidException(String message, Throwable cause, boolean enableSuppression,
                                              boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
