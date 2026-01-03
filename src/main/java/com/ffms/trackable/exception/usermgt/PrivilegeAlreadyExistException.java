package com.ffms.trackable.exception.usermgt;

public final class PrivilegeAlreadyExistException extends RuntimeException {

    private static final long serialVersionUID = 5861310537366287163L;

    public PrivilegeAlreadyExistException() {
        super();
    }

    public PrivilegeAlreadyExistException(final String message, final Throwable cause) {
        super(message, cause);
    }

    public PrivilegeAlreadyExistException(final String message) {
        super(message);
    }

    public PrivilegeAlreadyExistException(final Throwable cause) {
        super(cause);
    }

}
