package com.ffms.trackable.exception.usermgt;

public final class RoleAlreadyExistException extends RuntimeException {

    private static final long serialVersionUID = 5861310537366287163L;

    public RoleAlreadyExistException() {
        super();
    }

    public RoleAlreadyExistException(final String message, final Throwable cause) {
        super(message, cause);
    }

    public RoleAlreadyExistException(final String message) {
        super(message);
    }

    public RoleAlreadyExistException(final Throwable cause) {
        super(cause);
    }

}
