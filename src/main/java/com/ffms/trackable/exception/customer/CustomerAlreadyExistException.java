package com.ffms.trackable.exception.customer;

public final class CustomerAlreadyExistException extends RuntimeException {

    private static final long serialVersionUID = 5861310537366287163L;

    public CustomerAlreadyExistException() {
        super();
    }

    public CustomerAlreadyExistException(final String message, final Throwable cause) {
        super(message, cause);
    }

    public CustomerAlreadyExistException(final String message) {
        super(message);
    }

    public CustomerAlreadyExistException(final Throwable cause) {
        super(cause);
    }

    public static class EmailExistsException extends Throwable {
        public EmailExistsException(final String message) {
            super(message);
        }
    }
}
