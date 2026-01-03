package com.ffms.trackable.exception.security;

public final class RefreshTokenException extends RuntimeException {

    private static final long serialVersionUID = 5861310537366287163L;

    public RefreshTokenException() {
        super();
    }

    public RefreshTokenException(final String message, final Throwable cause) {
        super(message, cause);
    }

    public RefreshTokenException(final String message) {
        super(message);
    }

    public RefreshTokenException(final Throwable cause) {
        super(cause);
    }

}
