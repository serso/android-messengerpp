package org.solovyev.android.messenger.realms.xmpp;

import android.content.Context;
import android.util.Log;
import org.jivesoftware.smack.*;
import org.solovyev.android.messenger.AbstractRealmConnection;
import org.solovyev.android.messenger.realms.RealmIsNotConnectedException;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * User: serso
 * Date: 2/24/13
 * Time: 8:13 PM
 */
public class XmppRealmConnection extends AbstractRealmConnection<XmppRealm> {

    private static final String TAG = XmppRealmConnection.class.getSimpleName();

    @Nullable
    private Connection connection;

    @Nonnull
    private final ChatManagerListener chatListener;

    @Nonnull
    private final RosterListener rosterListener = new XmppRosterListener();

    public XmppRealmConnection(@Nonnull XmppRealm realm, @Nonnull Context context) {
        super(realm, context);
        chatListener = new XmppChatListener(realm);
    }

    @Override
    protected void doWork() throws ContextIsNotActiveException {
        connection = new XMPPConnection(getRealm().getConfiguration().toXmppConfiguration());

        connection.getChatManager().addChatListener(chatListener);
        connection.getRoster().addRosterListener(rosterListener);

        // loop guarantees that if something gone wrong we will initiate new XMPP connection
        while (!isStopped()) {
            if (connection != null) {

                // connect to the server
                try {
                    connection.connect();
                    if (!connection.isAuthenticated()) {
                        final XmppRealmConfiguration configuration = getRealm().getConfiguration();
                        connection.login(configuration.getLogin(), configuration.getPassword(), configuration.getResource());
                    }

                } catch (XMPPException e) {
                    Log.e(TAG, e.getMessage(), e);
                }
            } else {
                waitForLogin();
            }
        }

        if ( connection != null ) {
            connection.getRoster().removeRosterListener(rosterListener);
            connection.getChatManager().removeChatListener(chatListener);
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
