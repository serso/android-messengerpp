package org.solovyev.android.messenger.realms;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.solovyev.android.captcha.ResolvedCaptcha;
import org.solovyev.android.messenger.security.AuthData;
import org.solovyev.android.messenger.security.InvalidCredentialsException;
import org.solovyev.common.BuilderWithData;

public interface RealmBuilder extends BuilderWithData<Realm, RealmBuilder.Data> {

    @NotNull
    RealmDef getRealmDef();

    void connect() throws ConnectionException;

    void disconnect()throws ConnectionException;

    @NotNull
    AuthData loginUser(@Nullable ResolvedCaptcha resolvedCaptcha) throws InvalidCredentialsException;

    public static final class Data {

        @NotNull
        private final AuthData authData;

        @NotNull
        private final String realmId;

        public Data(@NotNull AuthData authData, @NotNull String realmId) {
            this.authData = authData;
            this.realmId = realmId;
        }

        @NotNull
        public AuthData getAuthData() {
            return authData;
        }

        @NotNull
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
