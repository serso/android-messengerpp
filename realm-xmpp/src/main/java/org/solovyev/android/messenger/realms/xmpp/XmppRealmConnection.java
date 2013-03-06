package org.solovyev.android.messenger.realms.xmpp;

import android.content.Context;
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
public class XmppRealmConnection extends AbstractRealmConnection<XmppRealm> implements XmppConnectionAware {

    private static final String TAG = XmppRealmConnection.class.getSimpleName();

    public static final int CONNECTION_RETRIES = 3;

    @Nullable
    private volatile Connection connection;

    @Nonnull
    private final ChatManagerListener chatListener;

    @Nullable
    private RosterListener rosterListener;

    public XmppRealmConnection(@Nonnull XmppRealm realm, @Nonnull Context context) {
        super(realm, context);
        chatListener = new XmppChatListener(realm);
    }

    @Override
    protected void doWork() throws ContextIsNotActiveException {
        tryToConnect(0);
    }

    private synchronized void tryToConnect(int connectionAttempt) {
        if (this.connection == null) {
            final Connection connection = new XMPPConnection(getRealm().getConfiguration().toXmppConfiguration());
            connection.getChatManager().addChatListener(chatListener);

            rosterListener = new XmppRosterListener(getRealm(), this);
            connection.getRoster().addRosterListener(rosterListener);

            // connect to the server
            try {
                prepareConnection(connection);

                this.connection = connection;
            } catch (XMPPException e) {
                if (connectionAttempt < CONNECTION_RETRIES) {
                    tryToConnect(connectionAttempt + 1);
                } else {
                    stop();
                }
            }
        }
    }

    private void prepareConnection(@Nonnull Connection connection) throws XMPPException {
        if (!connection.isConnected()) {
            connection.connect();
            if (!connection.isAuthenticated()) {
                final XmppRealmConfiguration configuration = getRealm().getConfiguration();
                connection.login(configuration.getLogin(), configuration.getPassword(), configuration.getResource());
            }
        }
    }

    @Override
    protected void stopWork() {
        if (connection != null) {
            if (rosterListener != null) {
                connection.getRoster().removeRosterListener(rosterListener);
            }
            connection.getChatManager().removeChatListener(chatListener);
            connection.disconnect();
            connection = null;
        }
    }

    @Nonnull
    private Connection tryGetConnection() throws XMPPException {
        if (connection != null) {
            prepareConnection(connection);
            return connection;
        } else {
            tryToConnect(CONNECTION_RETRIES - 1);
            if (connection != null) {
                return connection;
            } else {
                throw new RealmIsNotConnectedException();
            }
        }
    }

    @Override
    public <R> R doOnConnection(@Nonnull XmppConnectedCallable<R> callable) throws XMPPException {
        final Connection connection = tryGetConnection();
        synchronized (connection) {
            return callable.call(connection);
        }
    }
}
