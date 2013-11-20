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
import org.solovyev.android.messenger.accounts.Account;
import org.solovyev.android.messenger.sync.SyncService;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.GuardedBy;
import java.util.*;
import java.util.concurrent.Executor;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static java.util.concurrent.Executors.newCachedThreadPool;
import static java.util.concurrent.Executors.newSingleThreadScheduledExecutor;
import static org.solovyev.android.messenger.App.getSyncService;
import static org.solovyev.android.messenger.App.newTag;

/**
 * User: serso
 * Date: 3/6/13
 * Time: 10:47 PM
 */
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

	@Inject
	public DefaultAccountConnections(@Nonnull Context context) {
		this.context = context.getApplicationContext();
	}

	void setExecutor(@Nonnull Executor executor) {
		this.executor = executor;
	}

	@Override
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
				// after account connection is started we have to check user presences and new messages
				final SyncService syncService = getSyncService();

				// todo serso: investigate why this operation takes so long and uncomment after fix
				/*try {
					syncService.sync(SyncTask.user_contacts_statuses, null);
				} catch (TaskIsAlreadyRunningException e) {
				}*/

			}
		}, POST_START_DELAY, TimeUnit.SECONDS);
	}

	@Override
	public void tryStopAll() {
		synchronized (this.connections) {
			for (AccountConnection connection : connections) {
				if (!connection.isStopped()) {
					connection.stop();
				}
			}
		}
	}

	@Override
	public boolean onNoInternetConnection() {
		boolean stopped = false;

		synchronized (this.connections) {
			for (AccountConnection connection : connections) {
				if (connection.isInternetConnectionRequired() && !connection.isStopped()) {
					stopped = true;
					connection.stop();
				}
			}
		}

		return stopped;
	}

	@Override
	public void tryStopFor(@Nonnull Account account) {
		synchronized (this.connections) {
			for (AccountConnection connection : connections) {
				if (account.equals(connection.getAccount()) && !connection.isStopped()) {
					connection.stop();
				}
			}
		}
	}

	@Override
	public void tryStartAll(boolean internetConnectionExists) {
		synchronized (this.connections) {
			for (AccountConnection connection : connections) {
				if (connection.isStopped()) {
					if (!connection.isInternetConnectionRequired() || internetConnectionExists) {
						startConnection(connection);
					}
				}
			}
		}

		onConnectionsStarted();
	}

	@Override
	public void removeConnectionFor(@Nonnull Account account) {
		synchronized (this.connections) {
			// remove account connections belonged to specified realm
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

	@Override
	public void updateAccount(@Nonnull Account account, boolean internetConnectionExists) {
		synchronized (this.connections) {
			removeConnectionFor(account);
			startConnectionsFor(Arrays.asList(account), internetConnectionExists);
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
