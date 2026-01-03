package com.ffms.resqeats.exception.usermgt;

public class PrivilegeNotFoundException extends RuntimeException {

    private static final long serialVersionUID = 5861310537366287163L;

    public PrivilegeNotFoundException() {
        super();
    }

    public PrivilegeNotFoundException(final String message, final Throwable cause) {
        super(message, cause);
    }

    public PrivilegeNotFoundException(final String message) {
        super(message);
    }

    public PrivilegeNotFoundException(final Throwable cause) {
        super(cause);
    }

}
