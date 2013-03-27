package org.solovyev.android.messenger.realms;

import javax.annotation.Nonnull;

public class RealmException extends Exception {

    public RealmException() {
    }

    public RealmException(String detailMessage) {
        super(detailMessage);
    }

    public RealmException(String detailMessage, @Nonnull Throwable throwable) {
        super(detailMessage, throwable);
    }

    public RealmException(@Nonnull Throwable throwable) {
        super(throwable);
    }

    public RealmException(@Nonnull RealmRuntimeException exception) {
        super(unwrap(exception));
    }

    @Nonnull
    private static Throwable unwrap(@Nonnull RealmRuntimeException exception) {
        final Throwable cause = exception.getCause();
        return cause != null ? cause : exception;
    }

}
