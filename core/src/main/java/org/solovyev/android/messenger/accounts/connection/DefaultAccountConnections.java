/*
 * Copyright 2013 serso aka se.solovyev
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.solovyev.android.messenger.accounts.connection;

import android.content.Context;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.solovyev.android.PredicateSpy;
import org.solovyev.android.messenger.App;
import org.solovyev.android.messenger.Background;
import org.solovyev.android.messenger.accounts.Account;
import org.solovyev.android.messenger.sync.SyncAllTaskIsAlreadyRunning;
import org.solovyev.android.messenger.sync.SyncService;
import org.solovyev.android.messenger.sync.SyncTask;
import org.solovyev.android.messenger.sync.TaskIsAlreadyRunningException;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.GuardedBy;
import java.util.*;
import java.util.concurrent.Executor;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static com.google.common.collect.Iterables.find;
import static java.util.concurrent.Executors.newCachedThreadPool;
import static java.util.concurrent.Executors.newSingleThreadScheduledExecutor;
import static org.solovyev.android.messenger.App.getSyncService;
import static org.solovyev.android.messenger.App.newTag;

@Singleton
public final class DefaultAccountConnections implements AccountConnections {

    /*
	**********************************************************************
    *
    *                           CONSTANTS
    *
    **********************************************************************
    */

	// seconds
	private static final int POST_START_DELAY = 5;

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

	@Inject
	@Nonnull
	private Background background;

	@GuardedBy("connections")
	@Nonnull
	private final Set<AccountConnection> connections = new HashSet<AccountConnection>();

	@Nonnull
	private final AtomicInteger threadCounter = new AtomicInteger(0);

	@Nonnull
	private final ScheduledExecutorService postStartExecutor = newSingleThreadScheduledExecutor();

	@Nonnull
	private Executor executor = newCachedThreadPool(new ConnectionThreadFactory());

	@Inject
	public DefaultAccountConnections(@Nonnull Context context) {
		this.context = context.getApplicationContext();
	}

	void setExecutor(@Nonnull Executor executor) {
		this.executor = executor;
	}

	void setBackground(@Nonnull Background background) {
		this.background = background;
	}

	@Override
	public void startConnectionsFor(@Nonnull Collection<Account> accounts, boolean internetConnectionExists) {
		final Collection<Account> startedAccounts = new ArrayList<Account>(accounts.size());

		synchronized (connections) {
			for (final Account account : accounts) {
				// are there any connections for current account?
				AccountConnection connection = find(connections, new ConnectionFinder(account), null);

				if (connection == null) {
					// there is no connection for current account => need to add
					connection = account.newConnection(context);
					connections.add(connection);
				}

				if (connection.isStopped()) {
					if (internetConnectionExists || !connection.isInternetConnectionRequired()) {
						startConnection(connection);
						startedAccounts.add(account);
					}
				}
			}
		}

		onConnectionsStarted(startedAccounts);
	}

	private void startConnection(@Nonnull final AccountConnection connection) {
		executor.execute(new ConnectionRunnable(connection));
	}

	private void onConnectionsStarted(@Nonnull final Collection<Account> startedAccounts) {
		postStartExecutor.schedule(new Runnable() {
			@Override
			public void run() {
				final SyncService syncService = getSyncService();

				try {
					App.getSyncService().sync(SyncTask.user_contacts_statuses, null);
				} catch (TaskIsAlreadyRunningException e) {
					// don't care
				}

				try {
					App.getSyncService().sync(SyncTask.chat_messages, null);
				} catch (TaskIsAlreadyRunningException e) {
					// don't care
				}

				for (Account startedAccount : startedAccounts) {
					try {
						syncService.syncAllForAccount(startedAccount, false);
					} catch (SyncAllTaskIsAlreadyRunning syncAllTaskIsAlreadyRunning) {
						// don't care
					}
				}

			}
		}, POST_START_DELAY, TimeUnit.SECONDS);
	}

	@Override
	public void tryStopAll() {
		final List<AccountConnection> toBeStopped = new ArrayList<AccountConnection>();

		synchronized (this.connections) {
			for (AccountConnection connection : connections) {
				toBeStopped.add(connection);
			}
		}

		stopConnections(toBeStopped);
	}

	@Override
	public boolean onNoInternetConnection() {
		boolean stopped = false;

		final List<AccountConnection> toBeStopped = new ArrayList<AccountConnection>();

		synchronized (this.connections) {
			for (AccountConnection connection : connections) {
				if (connection.isInternetConnectionRequired()) {
					stopped = true;
					toBeStopped.add(connection);
				}
			}
		}

		for (AccountConnection connection : toBeStopped) {
			connection.stopDelayed();
		}

		background.getHighPriorityExecutor().execute(new Runnable() {
			@Override
			public void run() {
				stopConnections(toBeStopped);
			}
		});

		return stopped;
	}

	@Override
	public void tryStopFor(@Nonnull Account account) {
		final List<AccountConnection> toBeStopped = new ArrayList<AccountConnection>();

		synchronized (this.connections) {
			for (AccountConnection connection : this.connections) {
				if (account.equals(connection.getAccount())) {
					toBeStopped.add(connection);
				}
			}
		}

		stopConnections(toBeStopped);
	}

	@Override
	public void tryStartAll(boolean internetConnectionExists) {
		final Collection<Account> startedAccounts = new ArrayList<Account>(connections.size());

		synchronized (this.connections) {
			for (AccountConnection connection : connections) {
				if (connection.isStopped()) {
					if (!connection.isInternetConnectionRequired() || internetConnectionExists) {
						startConnection(connection);
						startedAccounts.add(connection.getAccount());
					}
				}
			}
		}

		onConnectionsStarted(startedAccounts);
	}

	@Override
	public void removeConnectionFor(@Nonnull Account account) {
		final List<AccountConnection> removedConnections = new ArrayList<AccountConnection>();

		synchronized (this.connections) {
			// remove account connections belonged to specified realm
			Iterables.removeIf(this.connections, PredicateSpy.spyOn(new ConnectionFinder(account), removedConnections));
		}

		stopConnections(removedConnections);
	}

	private void stopConnections(@Nonnull List<AccountConnection> connections) {
		assert !Thread.holdsLock(this.connections);

		for (AccountConnection connection : connections) {
			connection.stop();
		}
	}

	@Override
	public void restartConnectionForChangedAccount(@Nonnull Account account, boolean internetConnectionExists) {
		removeConnectionFor(account);

		synchronized (this.connections) {
			startConnectionsFor(Arrays.asList(account), internetConnectionExists);
		}
	}

	@Override
	public void updateAccount(@Nonnull Account account) {
		synchronized (this.connections) {
			final AccountConnection connection = find(this.connections, new ConnectionFinder(account), null);
			if (connection instanceof BaseAccountConnection) {
				((BaseAccountConnection) connection).setAccount(account);
			}
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
