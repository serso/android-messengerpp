package org.solovyev.android.messenger.realms;

import javax.annotation.Nonnull;

public class RealmConnectionException extends RealmException {

    public RealmConnectionException() {
    }

    public RealmConnectionException(String detailMessage) {
        super(detailMessage);
    }

    public RealmConnectionException(String detailMessage, @Nonnull Throwable throwable) {
        super(detailMessage, throwable);
    }

    public RealmConnectionException(@Nonnull Throwable throwable) {
        super(throwable);
    }

    public RealmConnectionException(@Nonnull RealmRuntimeException exception) {
        super(exception);
    }
}
