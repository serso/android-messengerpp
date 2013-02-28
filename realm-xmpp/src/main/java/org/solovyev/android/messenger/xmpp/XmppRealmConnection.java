package org.solovyev.android.messenger.xmpp;

import android.content.Context;
import android.util.Log;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jivesoftware.smack.Connection;
import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.solovyev.android.captcha.ResolvedCaptcha;
import org.solovyev.android.messenger.AbstractRealmConnection;
import org.solovyev.android.messenger.realms.RealmIsNotConnectedException;
import org.solovyev.android.messenger.security.AuthData;
import org.solovyev.android.messenger.security.AuthDataImpl;
import org.solovyev.android.messenger.security.InvalidCredentialsException;
import org.solovyev.android.messenger.security.RealmAuthService;
import org.solovyev.android.messenger.users.User;

/**
 * User: serso
 * Date: 2/24/13
 * Time: 8:13 PM
 */
public class XmppRealmConnection extends AbstractRealmConnection<XmppRealm> implements RealmAuthService {

    private static final String TAG = XmppRealmConnection.class.getSimpleName();

    @NotNull
    private ConnectionConfiguration configuration;

    @Nullable
    private Connection connection;

    public XmppRealmConnection(@NotNull XmppRealm realm, @NotNull Context context) {
        super(realm, context);
        configuration = realm.getConfiguration().toXmppConfiguration();
    }

    @Override
    protected void doWork() throws ContextIsNotActiveException {
        // loop guarantees that if something gone wrong we will initiate new XMPP connection
        while (!isStopped()) {
            final Connection connection = this.connection;
            if (connection != null) {

                // connect to the server
                try {
                    connection.connect();

                } catch (XMPPException e) {
                    Log.e(TAG, e.getMessage(), e);
                }
            } else {
                waitForLogin();
            }
        }
    }

    @NotNull
    @Override
    public AuthData loginUser(@Nullable ResolvedCaptcha resolvedCaptcha) throws InvalidCredentialsException {
       assert this.connection == null;

       final Connection connection = new XMPPConnection(configuration);

        try {
            final XmppRealmConfiguration configuration = getRealm().getConfiguration();
            connection.login(configuration.getLogin(), configuration.getPassword());

            final AuthDataImpl result = new AuthDataImpl();

            result.setRealmUserId(configuration.getLogin());
            result.setRealmUserLogin(configuration.getLogin());
            result.setAccessToken("");

            connection.getChatManager().addChatListener(new XmppChatListener());

            this.connection = connection;

            return result;
        } catch (XMPPException e) {
            throw new InvalidCredentialsException(e);
        }
    }

    @Override
    public void logoutUser(@NotNull User user) {
        if ( connection != null ) {
            connection.disconnect();
            connection = null;
        }
    }

    @NotNull
    public Connection getConnection() {
        if (connection != null) {
            return connection;
        } else {
            throw new RealmIsNotConnectedException();
        }
    }

}
