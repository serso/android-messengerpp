package org.solovyev.android.messenger.accounts.connection;

import android.content.Context;
import android.util.Log;
import org.solovyev.android.messenger.accounts.Account;
import org.solovyev.android.messenger.accounts.AccountConnectionException;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import java.util.concurrent.TimeUnit;

import static java.lang.Thread.currentThread;

public abstract class LoopedAccountConnection<A extends Account> extends AbstractAccountConnection<A> {

	private static final long DEFAULT_WAIT_MILLIS = 30L * 60L * 1000L;
	private static final long MIN_WAIT_MILLIS = 5L * 60L * 1000L;

	private final long waitMillis;

	@Nullable
	private volatile Thread thread;

	protected LoopedAccountConnection(@Nonnull A account, @Nonnull Context context, boolean internetConnectionRequired) {
		this(account, context, internetConnectionRequired, DEFAULT_WAIT_MILLIS);
	}
	protected LoopedAccountConnection(@Nonnull A account, @Nonnull Context context, boolean internetConnectionRequired, long waitMillis) {
		super(account, context, internetConnectionRequired);
		if(waitMillis < MIN_WAIT_MILLIS) {
			Log.w(TAG, "Too small connection wait time may lead to fast battery drain. Wait time should be more than 5 minutes.");
		}
		this.waitMillis = waitMillis;
	}

	protected LoopedAccountConnection(@Nonnull A account, @Nonnull Context context, boolean internetConnectionRequired, long waitTime, @Nonnull TimeUnit waitTimeUnit) {
		this(account, context, internetConnectionRequired, waitTimeUnit.toMillis(waitTime));
	}

	@Override
	protected final void start0() throws AccountConnectionException {
		thread = currentThread();

		// Try to create connection (if not exists)
		while (!isStopped()) {
			reconnectIfDisconnected();
			try {
				Thread.sleep(waitMillis);
			} catch (InterruptedException e) {
			}
		}

		thread = null;
	}

	public void continueLoop() {
		final Thread localThread = thread;
		if (localThread != null) {
			localThread.interrupt();
		}
	}

	@Override
	protected final void stop0() {
		disconnect();
	}

	protected abstract void disconnect();

	protected abstract void reconnectIfDisconnected() throws AccountConnectionException;
}
