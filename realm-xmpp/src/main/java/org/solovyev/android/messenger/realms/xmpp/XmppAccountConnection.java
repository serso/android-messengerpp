package org.solovyev.android.messenger.realms.xmpp;

import android.content.Context;
import org.jivesoftware.smack.*;
import org.jivesoftware.smackx.ChatStateManager;
import org.solovyev.android.messenger.accounts.connection.AbstractAccountConnection;
import org.solovyev.android.messenger.accounts.AccountConnectionException;
import org.solovyev.android.messenger.accounts.connection.LoopedAbstractAccountConnection;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * User: serso
 * Date: 2/24/13
 * Time: 8:13 PM
 */
public class XmppAccountConnection extends LoopedAbstractAccountConnection<XmppAccount> implements XmppConnectionAware {

	private static final String TAG = XmppAccountConnection.class.getSimpleName();

	private static final int CONNECTION_RETRIES = 3;

	@Nullable
	private volatile Connection connection;

	@Nonnull
	private final ChatManagerListener chatListener;

	@Nonnull
	private final RosterListener rosterListener;

	public XmppAccountConnection(@Nonnull XmppAccount account, @Nonnull Context context) {
		super(account, context);
		chatListener = new XmppChatListener(account);
		rosterListener = new XmppRosterListener(account);
	}

	@Override
	protected void tryConnect() throws AccountConnectionException {
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

	private void prepareConnection(@Nonnull Connection connection, @Nonnull XmppAccount realm) throws XMPPException {
		checkConnectionStatus(connection, realm);

		// todo serso: investigate why we cannot add listeners in after connection constructor
		// Attach listeners to connection
		connection.getChatManager().addChatListener(chatListener);
		connection.getRoster().addRosterListener(rosterListener);

		// init chat state manager (listeners will be added inside this method)
		ChatStateManager.getInstance(connection);
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
}
