package org.solovyev.android.messenger.security;

/**
 * User: serso
 * Date: 5/24/12
 * Time: 9:20 PM
 */
public class InvalidCredentialsException extends Exception {

    public InvalidCredentialsException() {
    }

    public InvalidCredentialsException(String detailMessage) {
        super(detailMessage);
    }

    public InvalidCredentialsException(String detailMessage, Throwable throwable) {
        super(detailMessage, throwable);
    }

    public InvalidCredentialsException(Throwable throwable) {
        super(throwable);
    }
}
