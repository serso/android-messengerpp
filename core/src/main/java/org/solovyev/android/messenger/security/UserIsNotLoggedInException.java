package org.solovyev.android.messenger.security;

/**
 * User: serso
 * Date: 5/30/12
 * Time: 11:27 PM
 */
public class UserIsNotLoggedInException extends Exception {

    public UserIsNotLoggedInException() {
    }

    public UserIsNotLoggedInException(String detailMessage) {
        super(detailMessage);
    }

    public UserIsNotLoggedInException(String detailMessage, Throwable throwable) {
        super(detailMessage, throwable);
    }

    public UserIsNotLoggedInException(Throwable throwable) {
        super(throwable);
    }
}
