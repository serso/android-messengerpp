package org.solovyev.android.messenger.realms.xmpp;

import android.content.Context;
import android.util.Log;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.jivesoftware.smack.AbstractConnectionListener;
import org.jivesoftware.smack.ChatManager;
import org.jivesoftware.smack.ChatManagerListener;
import org.jivesoftware.smack.Connection;
import org.jivesoftware.smack.ConnectionListener;
import org.jivesoftware.smack.Roster;
import org.jivesoftware.smack.RosterListener;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smackx.ChatStateManager;
import org.solovyev.android.messenger.accounts.AccountConnectionException;
import org.solovyev.android.messenger.accounts.connection.LoopedAccountConnection;

import static org.solovyev.android.messenger.App.getApplication;

/**
 * User: serso
 * Date: 2/24/13
 * Time: 8:13 PM
 */
public class XmppAccountConnection extends LoopedAccountConnection<XmppAccount> implements XmppConnectionAware {

	private static final int CONNECTION_RETRIES = 3;

	@Nullable
	private volatile Connection connection;

	@Nonnull
	private final ChatManagerListener chatListener;

	@Nonnull
	private final RosterListener rosterListener;

	@Nonnull
	private final ConnectionListener connectionListener = new XmppConnectionListener();

	public XmppAccountConnection(@Nonnull XmppAccount account, @Nonnull Context context) {
		super(account, context, true);
		chatListener = new XmppChatListener(account);
		rosterListener = new XmppRosterListener(account);
	}

	@Override
	protected void reconnectIfDisconnected() throws AccountConnectionException {
		if (this.connection == null) {
			tryToConnect(0);
		}
	}

	@Nullable
	private synchronized Connection tryToConnect(int connectionAttempt) throws AccountConnectionException {
		if (this.connection == null) {
			final XmppAccount account = getAccount();
			final Connection connection = new XMPPConnection(account.getConfiguration().toXmppConfiguration());

			// connect to the server
			try {
				prepareConnection(connection, account);

				this.connection = connection;
			} catch (XMPPException e) {
				if (connectionAttempt < CONNECTION_RETRIES) {
					tryToConnect(connectionAttempt + 1);
				} else {
					throw new AccountConnectionException(account.getId());
				}
			}
		}

		return this.connection;
	}

	private void prepareConnection(@Nonnull Connection connection, @Nonnull XmppAccount account) throws XMPPException {
		checkConnectionStatus(connection, account);

		if (connection.isConnected()) {
			connection.addConnectionListener(connectionListener);
			// todo serso: investigate why we cannot add listeners in after connection constructor
			// Attach listeners to connection
			connection.getChatManager().addChatListener(chatListener);
			connection.getRoster().addRosterListener(rosterListener);

			// init chat state manager (listeners will be added inside this method)
			ChatStateManager.getInstance(connection);
		}
	}

	static void checkConnectionStatus(@Nonnull Connection connection, @Nonnull XmppAccount realm) throws XMPPException {
		if (!connection.isConnected()) {
			connection.connect();
			if (!connection.isAuthenticated()) {
				final XmppAccountConfiguration configuration = realm.getConfiguration();
				connection.login(configuration.getLogin(), configuration.getPassword(), configuration.getResource());
			}
		}
	}

	@Override
	protected void disconnect() {
		Log.d(TAG, "Disconnecting from account: " + getAccount().getDisplayName(getApplication()));

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

			if (localConnection.isConnected()) {
				localConnection.disconnect();
			}
		}
		connection = null;
	}

	@Nonnull
	private Connection tryGetConnection() throws XMPPException, AccountConnectionException {
		Connection localConnection = connection;
		if (localConnection != null) {
			prepareConnection(localConnection, getAccount());
			return localConnection;
		} else {
			localConnection = tryToConnect(CONNECTION_RETRIES - 1);
			if (localConnection != null) {
				return localConnection;
			} else {
				throw new AccountConnectionException(getAccount().getId());
			}
		}
	}

	@Override
	public <R> R doOnConnection(@Nonnull XmppConnectedCallable<R> callable) throws XMPPException, AccountConnectionException {
		final Connection connection = tryGetConnection();
		synchronized (connection) {
			return callable.call(connection);
		}
	}

	private class XmppConnectionListener extends AbstractConnectionListener {
		@Override
		public void connectionClosedOnError(Exception e) {
			super.connectionClosedOnError(e);

			Log.e(TAG, e.getMessage(), e);

			disconnect();
			continueLoop();
		}
	}
}
