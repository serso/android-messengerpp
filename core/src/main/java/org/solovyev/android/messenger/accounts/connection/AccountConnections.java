package org.solovyev.android.messenger.accounts.connection;

import android.content.Context;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.GuardedBy;
import javax.annotation.concurrent.ThreadSafe;

import org.solovyev.android.PredicateSpy;
import org.solovyev.android.messenger.accounts.Account;
import org.solovyev.android.messenger.sync.SyncTask;
import org.solovyev.android.messenger.sync.TaskIsAlreadyRunningException;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;

import static java.util.concurrent.Executors.newCachedThreadPool;
import static java.util.concurrent.Executors.newSingleThreadScheduledExecutor;
import static org.solovyev.android.messenger.App.getSyncService;
import static org.solovyev.android.messenger.App.newTag;

/**
 * User: serso
 * Date: 3/6/13
 * Time: 10:47 PM
 */
@ThreadSafe
final class AccountConnections {

    /*
	**********************************************************************
    *
    *                           CONSTANTS
    *
    **********************************************************************
    */

	// seconds
	private static final int POST_START_DELAY = 20;

	static final String TAG = newTag("AccountConnections");

    /*
    **********************************************************************
    *
    *                           FIELDS
    *
    **********************************************************************
    */

	@Nonnull
	private final Context context;

	@GuardedBy("connections")
	@Nonnull
	private final Set<AccountConnection> connections = new HashSet<AccountConnection>();

	@Nonnull
	private final AtomicInteger threadCounter = new AtomicInteger(0);

	@Nonnull
	private final ScheduledExecutorService postStartExecutor = newSingleThreadScheduledExecutor();

	@Nonnull
	private Executor executor = newCachedThreadPool(new ConnectionThreadFactory());

	AccountConnections(@Nonnull Context context) {
		this.context = context.getApplicationContext();
	}

	void setExecutor(@Nonnull Executor executor) {
		this.executor = executor;
	}

	public void startConnectionsFor(@Nonnull Collection<Account> accounts, boolean internetConnectionExists) {
		synchronized (connections) {
			for (final Account account : accounts) {
				// are there any connections for current account?
				AccountConnection connection = Iterables.find(connections, new ConnectionFinder(account), null);

				if (connection == null) {
					// there is no connection for current account => need to add
					connection = account.newConnection(context);
					connections.add(connection);
				}

				if (connection.isStopped()) {
					if (internetConnectionExists || !connection.isInternetConnectionRequired()) {
						startConnection(connection);
					}
				}
			}
		}

		onConnectionsStarted();
	}

	private void startConnection(@Nonnull final AccountConnection connection) {
		executor.execute(new ConnectionRunnable(connection));
	}

	// todo serso: better approach is to fire "realm_connected" events from realm connection and do sync for each realm separately (as soon as it is connected)
	private void onConnectionsStarted() {
		postStartExecutor.schedule(new Runnable() {
			@Override
			public void run() {
				try {
					// after realm connection is started we have to check user presences
					getSyncService().sync(SyncTask.user_contacts_statuses, null);
				} catch (TaskIsAlreadyRunningException e) {
					// do not care
				}
			}
		}, POST_START_DELAY, TimeUnit.SECONDS);
	}

	public void tryStopAll() {
		synchronized (this.connections) {
			for (AccountConnection connection : connections) {
				if (!connection.isStopped()) {
					connection.stop();
				}
			}
		}
	}

	public void onNoInternetConnection() {
		synchronized (this.connections) {
			for (AccountConnection connection : connections) {
				if (connection.isInternetConnectionRequired() && !connection.isStopped()) {
					connection.stop();
				}
			}
		}
	}

	public void tryStopFor(@Nonnull Account account) {
		synchronized (this.connections) {
			for (AccountConnection connection : connections) {
				if (account.equals(connection.getAccount()) && !connection.isStopped()) {
					connection.stop();
				}
			}
		}
	}

	public void tryStartAll() {
		synchronized (this.connections) {
			for (AccountConnection connection : connections) {
				if (connection.isStopped()) {
					startConnection(connection);
				}
			}
		}

		onConnectionsStarted();
	}

	public void removeConnectionFor(@Nonnull Account account) {
		synchronized (this.connections) {
			// remove realm connections belonged to specified realm
			final List<AccountConnection> removedConnections = new ArrayList<AccountConnection>();
			Iterables.removeIf(this.connections, PredicateSpy.spyOn(new ConnectionFinder(account), removedConnections));

			// stop them
			for (AccountConnection removedConnection : removedConnections) {
				if (!removedConnection.isStopped()) {
					removedConnection.stop();
				}
			}
		}
	}

	public void updateAccount(@Nonnull Account account, boolean start) {
		synchronized (this.connections) {
			removeConnectionFor(account);
			startConnectionsFor(Arrays.asList(account), start);
		}
	}

	private static class ConnectionFinder implements Predicate<AccountConnection> {

		@Nonnull
		private final Account account;

		public ConnectionFinder(@Nonnull Account account) {
			this.account = account;
		}

		@Override
		public boolean apply(@Nullable AccountConnection connection) {
			return connection != null && connection.getAccount().equals(account);
		}
	}

	private class ConnectionThreadFactory implements ThreadFactory {

		@Nonnull
		@Override
		public Thread newThread(@Nonnull Runnable r) {
			return new Thread(r, "Account connection thread: " + threadCounter.incrementAndGet());
		}
	}
}
