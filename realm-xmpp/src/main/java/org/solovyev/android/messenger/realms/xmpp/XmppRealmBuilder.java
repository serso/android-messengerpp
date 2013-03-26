package org.solovyev.android.messenger.realms.xmpp;

import android.util.Log;
import org.jivesoftware.smack.Connection;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.solovyev.android.captcha.ResolvedCaptcha;
import org.solovyev.android.messenger.realms.AbstractRealmBuilder;
import org.solovyev.android.messenger.realms.Realm;
import org.solovyev.android.messenger.realms.RealmConfiguration;
import org.solovyev.android.messenger.realms.RealmDef;
import org.solovyev.android.messenger.realms.RealmState;
import org.solovyev.android.messenger.security.AuthData;
import org.solovyev.android.messenger.security.AuthDataImpl;
import org.solovyev.android.messenger.security.InvalidCredentialsException;
import org.solovyev.android.messenger.users.User;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class XmppRealmBuilder extends AbstractRealmBuilder {

    @Nonnull
    private XmppRealmConfiguration configuration;

    @Nullable
    private Connection connection;

    public XmppRealmBuilder(@Nonnull RealmDef realmDef,
                            @Nullable Realm editedRealm,
                            @Nonnull XmppRealmConfiguration configuration) {
        super(realmDef, editedRealm);
        this.configuration = configuration;
    }

    @Nullable
    @Override
    protected User getUserById(@Nonnull String realmId, @Nonnull String realmUserId) {
        User user;

        if ( connection != null ) {
            try {
                user = XmppRealmUserService.toUser(realmId, realmUserId, null, true, connection);
            } catch (XMPPException e) {
                Log.e("XmppRealmBuilder", e.getMessage(), e);
                user = null;
            }
        } else {
            user = null;
        }

        return user;
    }

    @Nonnull
    @Override
    protected Realm newRealm(@Nonnull String id, @Nonnull User user, @Nonnull RealmState state) {
        return new XmppRealm(id, getRealmDef(), user, configuration, state);
    }

    @Override
    public void connect() throws ConnectionException {
        connection = new XMPPConnection(configuration.toXmppConfiguration());

        try {
            connection.connect();
        } catch (IllegalStateException e) {
            throw new ConnectionException(e);
        } catch (XMPPException e) {
            throw new ConnectionException(e);
        }
    }

    @Override
    public void disconnect() throws ConnectionException {
        try {
            if (connection != null) {
                connection.disconnect();
            }
            connection = null;
        } catch (IllegalStateException e) {
            throw new ConnectionException(e);
        }
    }

    @Nonnull
    @Override
    public AuthData loginUser(@Nullable ResolvedCaptcha resolvedCaptcha) throws InvalidCredentialsException {
        try {
            if (connection != null) {
                connection.login(configuration.getLogin(), configuration.getPassword());

                final AuthDataImpl result = new AuthDataImpl();

                result.setRealmUserId(configuration.getLogin());
                result.setRealmUserLogin(configuration.getLogin());
                result.setAccessToken("");

                return result;
            } else {
                throw new InvalidCredentialsException("Not connected!");
            }
        } catch (XMPPException e) {
            throw new InvalidCredentialsException(e);
        }
    }

    @Nonnull
    @Override
    public RealmConfiguration getConfiguration() {
        return configuration;
    }
}
