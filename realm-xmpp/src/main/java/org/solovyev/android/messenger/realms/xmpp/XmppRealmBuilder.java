package org.solovyev.android.messenger.realms.xmpp;

import android.util.Log;
import org.jivesoftware.smack.Connection;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.solovyev.android.captcha.ResolvedCaptcha;
import org.solovyev.android.messenger.entities.EntityImpl;
import org.solovyev.android.messenger.realms.AbstractRealmBuilder;
import org.solovyev.android.messenger.realms.Realm;
import org.solovyev.android.messenger.realms.RealmDef;
import org.solovyev.android.messenger.realms.RealmState;
import org.solovyev.android.messenger.security.InvalidCredentialsException;
import org.solovyev.android.messenger.users.User;
import org.solovyev.android.messenger.users.Users;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class XmppRealmBuilder extends AbstractRealmBuilder<XmppRealmConfiguration> {;

    @Nullable
    private Connection connection;

    public XmppRealmBuilder(@Nonnull RealmDef realmDef,
                            @Nullable Realm editedRealm,
                            @Nonnull XmppRealmConfiguration configuration) {
        super(realmDef, editedRealm, configuration);
    }

    @Nonnull
    @Override
    protected User getRealmUser(@Nonnull String realmId) {
        User user;

        if ( connection != null ) {
            try {
                user = XmppRealmUserService.toUser(realmId, getConfiguration().getLogin(), null, true, connection);
            } catch (XMPPException e) {
                Log.e("XmppRealmBuilder", e.getMessage(), e);
                user = Users.newEmptyUser(EntityImpl.newInstance(realmId, getConfiguration().getLogin()));
            }
        } else {
            user = Users.newEmptyUser(EntityImpl.newInstance(realmId, getConfiguration().getLogin()));
        }

        return user;
    }

    @Nonnull
    @Override
    protected Realm newRealm(@Nonnull String id, @Nonnull User user, @Nonnull RealmState state) {
        return new XmppRealm(id, getRealmDef(), user, getConfiguration(), state);
    }

    @Override
    public void connect() throws ConnectionException {
        connection = new XMPPConnection(getConfiguration().toXmppConfiguration());

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

    @Override
    public void loginUser(@Nullable ResolvedCaptcha resolvedCaptcha) throws InvalidCredentialsException {
        try {
            if (connection != null) {
                final XmppRealmConfiguration configuration = getConfiguration();
                connection.login(configuration.getLogin(), configuration.getPassword());
            } else {
                throw new InvalidCredentialsException("Not connected!");
            }
        } catch (XMPPException e) {
            throw new InvalidCredentialsException(e);
        }
    }
}
