package org.solovyev.android.messenger.accounts.connection;

import android.content.Context;
import android.util.Log;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import org.solovyev.android.PredicateSpy;
import org.solovyev.android.messenger.accounts.connection.AccountConnection;
import org.solovyev.android.messenger.MessengerApplication;
import org.solovyev.android.messenger.accounts.Account;
import org.solovyev.android.messenger.accounts.AccountConnectionException;
import org.solovyev.android.messenger.accounts.AccountState;
import org.solovyev.android.messenger.sync.SyncTask;
import org.solovyev.android.messenger.sync.TaskIsAlreadyRunningException;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.GuardedBy;
import javax.annotation.concurrent.ThreadSafe;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

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

	private static final int RETRY_CONNECTION_ATTEMPT_COUNT = 5;

	// seconds
	private static final int POST_START_DELAY = 3;

    /*
    **********************************************************************
    *
    *                           FIELDS
    *
    **********************************************************************
    */

	@Nonnull
	private final Context context;

	@GuardedBy("realmConnections")
	@Nonnull
	private final Set<AccountConnection> accountConnections = new HashSet<AccountConnection>();

	@Nonnull
	private final AtomicInteger threadCounter = new AtomicInteger(0);

	@Nonnull
	private final ScheduledExecutorService postStartExecutor = Executors.newSingleThreadScheduledExecutor();

	@Nonnull
	private final ExecutorService executor = Executors.newCachedThreadPool(new ThreadFactory() {

		@Override
		public Thread newThread(Runnable r) {
			return new Thread(r, "Account connection thread: " + threadCounter.incrementAndGet());
		}
	});

	AccountConnections(@Nonnull Context context) {
		this.context = context.getApplicationContext();
	}

	public void startConnectionsFor(@Nonnull Collection<Account> accounts, boolean start) {
		synchronized (accountConnections) {
			for (final Account account : accounts) {
				// are there any realm connections for current realm?
				boolean contains = Iterables.any(accountConnections, new AccountConnectionFinder(account));

				if (!contains) {
					// there is no realm connection for current realm => need to add
					final AccountConnection accountConnection = account.newRealmConnection(context);

					accountConnections.add(accountConnection);

					if (start) {
						startAccountConnection(accountConnection);
					}
				}
			}
		}

		onAccountConnectionsStarted();
	}

	private void startAccountConnection(@Nonnull final AccountConnection accountConnection) {
		executor.execute(new Runnable() {
			@Override
			public void run() {
				startAccountConnection(0, null);
			}

			private void startAccountConnection(int attempt, @Nullable AccountConnectionException lastError) {
				Log.d(MessengerApplication.TAG, "Realm start requested, attempt: " + attempt);

				if (attempt > RETRY_CONNECTION_ATTEMPT_COUNT) {
					Log.d(MessengerApplication.TAG, "Max retry count reached => stopping...");

					if (!accountConnection.isStopped()) {
						accountConnection.stop();
					}

					if (lastError != null) {
						MessengerApplication.getServiceLocator().getExceptionHandler().handleException(lastError);
					}

					MessengerApplication.getServiceLocator().getAccountService().changeAccountState(accountConnection.getAccount(), AccountState.disabled_by_app);
				} else {
					if (accountConnection.isStopped()) {
						try {
							if (accountConnection.getAccount().isEnabled()) {
								Log.d(MessengerApplication.TAG, "Realm is enabled => starting connection...");
								accountConnection.start();
							}
						} catch (AccountConnectionException e) {
							Log.w(MessengerApplication.TAG, "Realm connection error occurred, connection attempt: " + attempt, e);

							if (!accountConnection.isStopped()) {
								accountConnection.stop();
							}

							try {
								// let's wait a little bit - may be the exception was caused by connectivity problem
								Thread.sleep(5000);
							} catch (InterruptedException e1) {
								Log.e(MessengerApplication.TAG, e1.getMessage(), e1);
							} finally {
								startAccountConnection(attempt + 1, e);
							}
						}
					}
				}
			}
		});
	}

	// todo serso: better approach is to fire "realm_connected" events from realm connection and do sync for each realm separately (as soon as it is connected)
	private void onAccountConnectionsStarted() {
		postStartExecutor.schedule(new Runnable() {
			@Override
			public void run() {
				try {
					// after realm connection is started we have to check user presences
					MessengerApplication.getServiceLocator().getSyncService().sync(SyncTask.user_contacts_statuses, null);
				} catch (TaskIsAlreadyRunningException e) {
					// do not care
				}
			}
		}, POST_START_DELAY, TimeUnit.SECONDS);
	}

	public void tryStopAll() {
		synchronized (this.accountConnections) {
			for (AccountConnection accountConnection : accountConnections) {
				if (!accountConnection.isStopped()) {
					accountConnection.stop();
				}
			}
		}
	}

	public void tryStopFor(@Nonnull Account account) {
		synchronized (this.accountConnections) {
			for (AccountConnection accountConnection : accountConnections) {
				if (account.equals(accountConnection.getAccount()) && !accountConnection.isStopped()) {
					accountConnection.stop();
				}
			}
		}
	}

	public void tryStartAll() {
		synchronized (this.accountConnections) {
			for (AccountConnection accountConnection : accountConnections) {
				if (accountConnection.isStopped()) {
					startAccountConnection(accountConnection);
				}
			}
		}

		onAccountConnectionsStarted();
	}

	public void removeConnectionFor(@Nonnull Account account) {
		synchronized (this.accountConnections) {
			// remove realm connections belonged to specified realm
			final List<AccountConnection> removedConnections = new ArrayList<AccountConnection>();
			Iterables.removeIf(this.accountConnections, PredicateSpy.spyOn(new AccountConnectionFinder(account), removedConnections));

			// stop them
			for (AccountConnection removedConnection : removedConnections) {
				if (!removedConnection.isStopped()) {
					removedConnection.stop();
				}
			}
		}
	}

	public void updateAccount(@Nonnull Account account, boolean start) {
		synchronized (this.accountConnections) {
			removeConnectionFor(account);
			startConnectionsFor(Arrays.asList(account), start);
		}
	}

	private static class AccountConnectionFinder implements Predicate<AccountConnection> {

		@Nonnull
		private final Account account;

		public AccountConnectionFinder(@Nonnull Account account) {
			this.account = account;
		}

		@Override
		public boolean apply(@Nullable AccountConnection accountConnection) {
			return accountConnection != null && accountConnection.getAccount().equals(account);
		}
	}

}
