package org.solovyev.android.messenger.realms.xmpp;

import android.content.Context;
import android.util.Log;
import org.jivesoftware.smack.Connection;
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

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * User: serso
 * Date: 2/24/13
 * Time: 8:13 PM
 */
public class XmppRealmConnection extends AbstractRealmConnection<XmppRealm> implements RealmAuthService {

    private static final String TAG = XmppRealmConnection.class.getSimpleName();

    @Nullable
    private Connection connection;

    @Nonnull
    private final XmppChatListener chatListener = new XmppChatListener();

    public XmppRealmConnection(@Nonnull XmppRealm realm, @Nonnull Context context) {
        super(realm, context);
    }

    @Override
    protected void doWork() throws ContextIsNotActiveException {
        connection = new XMPPConnection(getRealm().getConfiguration().toXmppConfiguration());
        connection.getChatManager().addChatListener(chatListener);

        // loop guarantees that if something gone wrong we will initiate new XMPP connection
        while (!isStopped()) {
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

    @Nonnull
    @Override
    public AuthData loginUser(@Nullable ResolvedCaptcha resolvedCaptcha) throws InvalidCredentialsException {
       assert this.connection == null;

       final Connection connection = new XMPPConnection(getRealm().getConfiguration().toXmppConfiguration());

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
    public void logoutUser(@Nonnull User user) {
        if ( connection != null ) {
            connection.disconnect();
            connection = null;
        }
    }

    @Nonnull
    public Connection getConnection() {
        if (connection != null) {
            return connection;
        } else {
            throw new RealmIsNotConnectedException();
        }
    }

}
