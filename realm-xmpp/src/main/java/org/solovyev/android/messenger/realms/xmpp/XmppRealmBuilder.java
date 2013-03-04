package org.solovyev.android.messenger.realms.xmpp;

import org.jivesoftware.smack.AccountManager;
import org.jivesoftware.smack.Connection;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.solovyev.android.captcha.ResolvedCaptcha;
import org.solovyev.android.messenger.realms.*;
import org.solovyev.android.messenger.security.AuthData;
import org.solovyev.android.messenger.security.AuthDataImpl;
import org.solovyev.android.messenger.security.InvalidCredentialsException;
import org.solovyev.android.messenger.users.User;
import org.solovyev.android.messenger.users.UserImpl;
import org.solovyev.android.messenger.users.UserSyncDataImpl;
import org.solovyev.android.properties.AProperty;
import org.solovyev.android.properties.APropertyImpl;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;

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
            final ArrayList<AProperty> properties = new ArrayList<AProperty>();

            final AccountManager accountManager = connection.getAccountManager();

            for (String attributeName : accountManager.getAccountAttributes()) {
                final String attributeValue = accountManager.getAccountAttribute(attributeName);
                properties.add(APropertyImpl.newInstance(attributeName, attributeValue));
            }

            user = UserImpl.newInstance(RealmEntityImpl.newInstance(realmId, realmUserId), UserSyncDataImpl.newNeverSyncedInstance(), properties);
        } else {
            user = null;
        }

        return user;
    }

    @Nonnull
    @Override
    protected Realm newRealm(@Nonnull String id, @Nonnull User user) {
        return new XmppRealm(id, getRealmDef(), user, configuration);
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
        // already logged in
        //connection.login(configuration.getLogin(), configuration.getPassword());

        final AuthDataImpl result = new AuthDataImpl();

        result.setRealmUserId(configuration.getLogin());
        result.setRealmUserLogin(configuration.getLogin());
        result.setAccessToken("");

        return result;

    }

    @Nonnull
    @Override
    public RealmConfiguration getConfiguration() {
        return configuration;
    }
}
