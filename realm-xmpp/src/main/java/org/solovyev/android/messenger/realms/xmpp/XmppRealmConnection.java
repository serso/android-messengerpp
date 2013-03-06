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
        // loop guarantees that if something gone wrong we will initiate new XMPP connection
        while (!isStopped()) {
            if (connection != null) {

                // connect to the server
                try {
                    if (!connection.isConnected()) {
                        connection.connect();
                        if (!connection.isAuthenticated()) {
                            final XmppRealmConfiguration configuration = getRealm().getConfiguration();
                            connection.login(configuration.getLogin(), configuration.getPassword(), configuration.getResource());
                        }
                    }

                    // sleep one minute
                    Thread.sleep(60L * 1000L);
                } catch (XMPPException e) {
                    stopWork();
                } catch (InterruptedException e) {
                    stopWork();
                }
            } else {
                connection = new XMPPConnection(getRealm().getConfiguration().toXmppConfiguration());

                connection.getChatManager().addChatListener(chatListener);

                rosterListener = new XmppRosterListener(getRealm(), this);
                connection.getRoster().addRosterListener(rosterListener);
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
    public Connection getConnection() {
        if (connection != null) {
            return connection;
        } else {
            throw new RealmIsNotConnectedException();
        }
    }

    @Override
    public <R> R doOnConnection(@Nonnull XmppConnectedCallable<R> callable) throws XMPPException {
        return callable.call(getConnection());
    }
}
