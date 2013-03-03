package org.solovyev.android.messenger.realms;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.solovyev.android.captcha.ResolvedCaptcha;
import org.solovyev.android.messenger.security.AuthData;
import org.solovyev.android.messenger.security.InvalidCredentialsException;
import org.solovyev.common.BuilderWithData;

public interface RealmBuilder extends BuilderWithData<Realm, RealmBuilder.Data> {

    @Nonnull
    RealmDef getRealmDef();

    @Nullable
    Realm getEditedRealm();

    void connect() throws ConnectionException;

    void disconnect()throws ConnectionException;

    @Nonnull
    AuthData loginUser(@Nullable ResolvedCaptcha resolvedCaptcha) throws InvalidCredentialsException;

    @Nonnull
    RealmConfiguration getConfiguration();

    public static final class Data {

        @Nonnull
        private final AuthData authData;

        @Nonnull
        private final String realmId;

        public Data(@Nonnull AuthData authData, @Nonnull String realmId) {
            this.authData = authData;
            this.realmId = realmId;
        }

        @Nonnull
        public AuthData getAuthData() {
            return authData;
        }

        @Nonnull
        public String getRealmId() {
            return realmId;
        }
    }

    public static class ConnectionException extends Exception {

        public ConnectionException() {
        }

        public ConnectionException(Throwable throwable) {
            super(throwable);
        }
    }
}
