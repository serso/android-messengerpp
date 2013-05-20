package org.solovyev.android.messenger;

import android.content.Context;
import org.solovyev.android.messenger.realms.Realm;
import org.solovyev.android.messenger.realms.RealmConnectionException;

import javax.annotation.Nonnull;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * User: serso
 * Date: 7/25/12
 * Time: 6:10 PM
 */
public abstract class AbstractRealmConnection<R extends Realm> implements RealmConnection {

	@Nonnull
	private static final String TAG = "RealmConnection";

	@Nonnull
	private volatile R realm;

	@Nonnull
	private final Context context;

	@Nonnull
	private final AtomicBoolean stopped = new AtomicBoolean(true);

	protected AbstractRealmConnection(@Nonnull R realm, @Nonnull Context context) {
		this.realm = realm;
		this.context = context;
	}

	@Nonnull
	public final R getRealm() {
		return realm;
	}

	@Nonnull
	protected Context getContext() {
		return context;
	}

	public boolean isStopped() {
		return stopped.get();
	}

	@Override
	public final void start() throws RealmConnectionException {
		stopped.set(false);
		try {
			doWork();
		} finally {
			stop();
		}
	}

	protected abstract void doWork() throws RealmConnectionException;

	protected abstract void stopWork();

	@Override
	public final void stop() {
		stopped.set(true);
		stopWork();
	}
}
