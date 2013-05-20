package org.solovyev.android.messenger.realms;

import android.content.Context;
import android.util.Log;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import org.solovyev.android.PredicateSpy;
import org.solovyev.android.messenger.MessengerApplication;
import org.solovyev.android.messenger.RealmConnection;
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
final class RealmConnections {

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
	private final Set<RealmConnection> realmConnections = new HashSet<RealmConnection>();

	@Nonnull
	private final AtomicInteger threadCounter = new AtomicInteger(0);

	@Nonnull
	private final ScheduledExecutorService postStartExecutor = Executors.newSingleThreadScheduledExecutor();

	@Nonnull
	private final ExecutorService executor = Executors.newCachedThreadPool(new ThreadFactory() {

		@Override
		public Thread newThread(Runnable r) {
			return new Thread(r, "Realm connection thread: " + threadCounter.incrementAndGet());
		}
	});

	RealmConnections(@Nonnull Context context) {
		this.context = context.getApplicationContext();
	}

	public void startConnectionsFor(@Nonnull Collection<Realm> realms, boolean start) {
		synchronized (realmConnections) {
			for (final Realm realm : realms) {
				// are there any realm connections for current realm?
				boolean contains = Iterables.any(realmConnections, new RealmConnectionFinder(realm));

				if (!contains) {
					// there is no realm connection for current realm => need to add
					final RealmConnection realmConnection = realm.newRealmConnection(context);

					realmConnections.add(realmConnection);

					if (start) {
						startRealmConnection(realmConnection);
					}
				}
			}
		}

		onRealmConnectionsStarted();
	}

	private void startRealmConnection(@Nonnull final RealmConnection realmConnection) {
		executor.execute(new Runnable() {
			@Override
			public void run() {
				startRealmConnection(0, null);
			}

			private void startRealmConnection(int attempt, @Nullable RealmConnectionException lastError) {
				Log.d(MessengerApplication.TAG, "Realm start requested, attempt: " + attempt);

				if (attempt > RETRY_CONNECTION_ATTEMPT_COUNT) {
					Log.d(MessengerApplication.TAG, "Max retry count reached => stopping...");

					if (!realmConnection.isStopped()) {
						realmConnection.stop();
					}

					if (lastError != null) {
						MessengerApplication.getServiceLocator().getExceptionHandler().handleException(lastError);
					}

					MessengerApplication.getServiceLocator().getRealmService().changeRealmState(realmConnection.getRealm(), RealmState.disabled_by_app);
				} else {
					if (realmConnection.isStopped()) {
						try {
							if (realmConnection.getRealm().isEnabled()) {
								Log.d(MessengerApplication.TAG, "Realm is enabled => starting connection...");
								realmConnection.start();
							}
						} catch (RealmConnectionException e) {
							Log.w(MessengerApplication.TAG, "Realm connection error occurred, connection attempt: " + attempt, e);

							if (!realmConnection.isStopped()) {
								realmConnection.stop();
							}

							try {
								// let's wait a little bit - may be the exception was caused by connectivity problem
								Thread.sleep(5000);
							} catch (InterruptedException e1) {
								Log.e(MessengerApplication.TAG, e1.getMessage(), e1);
							} finally {
								startRealmConnection(attempt + 1, e);
							}
						}
					}
				}
			}
		});
	}

	// todo serso: better approach is to fire "realm_connected" events from realm connection and do sync for each realm separately (as soon as it is connected)
	private void onRealmConnectionsStarted() {
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
		synchronized (this.realmConnections) {
			for (RealmConnection realmConnection : realmConnections) {
				if (!realmConnection.isStopped()) {
					realmConnection.stop();
				}
			}
		}
	}

	public void tryStopFor(@Nonnull Realm realm) {
		synchronized (this.realmConnections) {
			for (RealmConnection realmConnection : realmConnections) {
				if (realm.equals(realmConnection.getRealm()) && !realmConnection.isStopped()) {
					realmConnection.stop();
				}
			}
		}
	}

	public void tryStartAll() {
		synchronized (this.realmConnections) {
			for (RealmConnection realmConnection : realmConnections) {
				if (realmConnection.isStopped()) {
					startRealmConnection(realmConnection);
				}
			}
		}

		onRealmConnectionsStarted();
	}

	public void removeConnectionFor(@Nonnull Realm realm) {
		synchronized (this.realmConnections) {
			// remove realm connections belonged to specified realm
			final List<RealmConnection> removedConnections = new ArrayList<RealmConnection>();
			Iterables.removeIf(this.realmConnections, PredicateSpy.spyOn(new RealmConnectionFinder(realm), removedConnections));

			// stop them
			for (RealmConnection removedConnection : removedConnections) {
				if (!removedConnection.isStopped()) {
					removedConnection.stop();
				}
			}
		}
	}

	public void updateRealm(@Nonnull Realm realm, boolean start) {
		synchronized (this.realmConnections) {
			removeConnectionFor(realm);
			startConnectionsFor(Arrays.asList(realm), start);
		}
	}

	private static class RealmConnectionFinder implements Predicate<RealmConnection> {

		@Nonnull
		private final Realm realm;

		public RealmConnectionFinder(@Nonnull Realm realm) {
			this.realm = realm;
		}

		@Override
		public boolean apply(@Nullable RealmConnection realmConnection) {
			return realmConnection != null && realmConnection.getRealm().equals(realm);
		}
	}

}
