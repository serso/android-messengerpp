package org.solovyev.android.messenger.accounts.connection;

import android.content.Context;
import org.solovyev.android.messenger.accounts.Account;
import org.solovyev.android.messenger.accounts.AccountConnectionException;

import javax.annotation.Nonnull;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * User: serso
 * Date: 7/25/12
 * Time: 6:10 PM
 */
public abstract class AbstractAccountConnection<A extends Account> implements AccountConnection {

	@Nonnull
	private static final String TAG = "AccountConnection";

	@Nonnull
	private volatile A account;

	@Nonnull
	private final Context context;

	@Nonnull
	private final AtomicBoolean stopped = new AtomicBoolean(true);

	protected AbstractAccountConnection(@Nonnull A account, @Nonnull Context context) {
		this.account = account;
		this.context = context;
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
		stopped.set(true);
		stopWork();
	}
}
