package org.solovyev.android.messenger.realms;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jivesoftware.smack.AccountManager;
import org.jivesoftware.smack.Connection;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.solovyev.android.captcha.ResolvedCaptcha;
import org.solovyev.android.messenger.security.AuthData;
import org.solovyev.android.messenger.security.AuthDataImpl;
import org.solovyev.android.messenger.security.InvalidCredentialsException;
import org.solovyev.android.messenger.users.User;
import org.solovyev.android.messenger.users.UserImpl;
import org.solovyev.android.messenger.users.UserSyncDataImpl;
import org.solovyev.android.messenger.xmpp.XmppRealm;
import org.solovyev.android.messenger.xmpp.XmppRealmConfiguration;
import org.solovyev.android.properties.AProperty;
import org.solovyev.android.properties.APropertyImpl;

import java.util.ArrayList;

public class XmppRealmBuilder extends AbstractRealmBuilder {

    @NotNull
    private XmppRealmConfiguration configuration;

    @Nullable
    private Connection connection;

    public XmppRealmBuilder(@NotNull RealmDef realmDef,
                            @Nullable Realm editedRealm,
                            @NotNull XmppRealmConfiguration configuration) {
        super(realmDef, editedRealm);
        this.configuration = configuration;
    }

    @Nullable
    @Override
    protected User getUserById(@NotNull String realmId, @NotNull String realmUserId) {
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

    @NotNull
    @Override
    protected Realm newRealm(@NotNull String id, @NotNull User user) {
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

    @NotNull
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

    @NotNull
    @Override
    public RealmConfiguration getConfiguration() {
        return configuration;
    }
}
