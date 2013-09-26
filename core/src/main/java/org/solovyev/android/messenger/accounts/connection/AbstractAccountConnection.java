package org.solovyev.android.messenger.accounts.connection;

import android.content.Context;

import java.util.concurrent.atomic.AtomicBoolean;

import javax.annotation.Nonnull;

import org.solovyev.android.messenger.accounts.Account;
import org.solovyev.android.messenger.accounts.AccountConnectionException;

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
		stopped.set(false);
		try {
			doWork();
		} finally {
			stop();
		}
	}

	protected abstract void doWork() throws AccountConnectionException;

	protected abstract void stopWork();

	@Override
	public final void stop() {
		if (stopped.compareAndSet(false, true)) {
			stopWork();
		}
	}

	@Override
	public boolean isInternetConnectionRequired() {
		return internetConnectionRequired;
	}
}
