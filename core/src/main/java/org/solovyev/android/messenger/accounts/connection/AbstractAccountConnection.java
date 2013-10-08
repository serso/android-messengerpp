package org.solovyev.android.messenger.accounts.connection;

import android.content.Context;
import android.util.Log;

import org.solovyev.android.messenger.accounts.Account;
import org.solovyev.android.messenger.accounts.AccountConnectionException;

import javax.annotation.Nonnull;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.solovyev.android.messenger.App.TAG;
import static org.solovyev.android.messenger.App.newTag;

/**
 * User: serso
 * Date: 7/25/12
 * Time: 6:10 PM
 */
public abstract class AbstractAccountConnection<A extends Account> implements AccountConnection {

	@Nonnull
	protected final String TAG = newTag(getClass().getName());

	@Nonnull
	private volatile A account;

	@Nonnull
	private final Context context;

	@Nonnull
	private final AtomicBoolean stopped = new AtomicBoolean(true);

	private final boolean internetConnectionRequired;

	protected AbstractAccountConnection(@Nonnull A account, @Nonnull Context context, boolean internetConnectionRequired) {
		this.account = account;
		this.context = context;
		this.internetConnectionRequired = internetConnectionRequired;
	}

	@Nonnull
	public final A getAccount() {
		return account;
	}

	@Nonnull
	protected Context getContext() {
		return context;
	}

	public boolean isStopped() {
		return stopped.get();
	}

	@Override
	public final void start() throws AccountConnectionException {
		if(stopped.compareAndSet(true, false)) {
			Log.d(TAG, "Trying to start connection");
			start0();
		} else {
			Log.d(TAG, "Connection is already started");
		}
	}

	protected abstract void start0() throws AccountConnectionException;

	protected abstract void stop0();

	@Override
	public final void stop() {
		if (stopped.compareAndSet(false, true)) {
			Log.d(TAG, "Trying to stop connection");
			stop0();
		} else {
			Log.d(TAG, "Connection is already stopped");
		}
	}

	@Override
	public boolean isInternetConnectionRequired() {
		return internetConnectionRequired;
	}
}
