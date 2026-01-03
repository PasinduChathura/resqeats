package com.ffms.resqeats.exception.usermgt;

public class RoleNotFoundException extends RuntimeException {

    private static final long serialVersionUID = 5861310537366287163L;

    public RoleNotFoundException() {
        super();
    }

    public RoleNotFoundException(final String message, final Throwable cause) {
        super(message, cause);
    }

    public RoleNotFoundException(final String message) {
        super(message);
    }

    public RoleNotFoundException(final Throwable cause) {
        super(cause);
    }

}
