package org.solovyev.android.messenger.realms.xmpp;

import android.content.Context;
import org.jivesoftware.smack.*;
import org.jivesoftware.smackx.ChatStateManager;
import org.solovyev.android.messenger.AbstractRealmConnection;
import org.solovyev.android.messenger.realms.RealmConnectionException;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * User: serso
 * Date: 2/24/13
 * Time: 8:13 PM
 */
public class XmppRealmConnection extends AbstractRealmConnection<XmppRealm> implements XmppConnectionAware {

    private static final String TAG = XmppRealmConnection.class.getSimpleName();

    private static final int CONNECTION_RETRIES = 3;

    @Nullable
    private volatile Connection connection;

    @Nonnull
    private final ChatManagerListener chatListener;

    @Nonnull
    private final RosterListener rosterListener;

    public XmppRealmConnection(@Nonnull XmppRealm realm, @Nonnull Context context) {
        super(realm, context);
        chatListener = new XmppChatListener(realm);
        rosterListener = new XmppRosterListener(realm);
    }

    @Override
    protected void doWork() throws RealmConnectionException {
        // Try to create connection (if not exists)
        if (this.connection == null) {
            tryToConnect(0);
        }
    }

    private synchronized void tryToConnect(int connectionAttempt) throws RealmConnectionException {
        if (this.connection == null) {
            final Connection connection = new XMPPConnection(getRealm().getConfiguration().toXmppConfiguration());

            // connect to the server
            try {
                prepareConnection(connection, getRealm());

                this.connection = connection;
            } catch (XMPPException e) {
                if (connectionAttempt < CONNECTION_RETRIES) {
                    tryToConnect(connectionAttempt + 1);
                } else {
                    throw new RealmConnectionException("Unable to connect!");
                }
            }
        }
    }

    private void prepareConnection(@Nonnull Connection connection, @Nonnull XmppRealm realm) throws XMPPException {
        checkConnectionStatus(connection, realm);

        // todo serso: investigate why we cannot add listeners in after connection constructor
        // Attach listeners to connection
        connection.getChatManager().addChatListener(chatListener);
        connection.getRoster().addRosterListener(rosterListener);

        // init chat state manager (listeners will be added inside this method)
        ChatStateManager.getInstance(connection);
    }

    static void checkConnectionStatus(@Nonnull Connection connection, @Nonnull XmppRealm realm) throws XMPPException {
        if (!connection.isConnected()) {
            connection.connect();
            if (!connection.isAuthenticated()) {
                final XmppRealmConfiguration configuration = realm.getConfiguration();
                connection.login(configuration.getLogin(), configuration.getPassword(), configuration.getResource());
            }
        }
    }

    @Override
    protected void stopWork() {
        final Connection localConnection = connection;
        if (localConnection != null) {
            final Roster roster = localConnection.getRoster();
            if (roster != null) {
                roster.removeRosterListener(rosterListener);
            }
            final ChatManager chatManager = localConnection.getChatManager();
            if (chatManager != null) {
                chatManager.removeChatListener(chatListener);
            }
            localConnection.disconnect();
        }
        connection = null;
    }

    @Nonnull
    private Connection tryGetConnection() throws XMPPException, RealmConnectionException {
        if (connection != null) {
            prepareConnection(connection, getRealm());
            return connection;
        } else {
            tryToConnect(CONNECTION_RETRIES - 1);
            if (connection != null) {
                return connection;
            } else {
                throw new RealmConnectionException();
            }
        }
    }

    @Override
    public <R> R doOnConnection(@Nonnull XmppConnectedCallable<R> callable) throws XMPPException, RealmConnectionException {
        final Connection connection = tryGetConnection();
        synchronized (connection) {
            return callable.call(connection);
        }
    }
}
