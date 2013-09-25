package org.solovyev.android.messenger.accounts.connection;

import android.content.Context;

import javax.annotation.Nonnull;

import org.solovyev.android.messenger.accounts.Account;
import org.solovyev.android.messenger.accounts.AccountConnectionException;

public abstract class LoopedAbstractAccountConnection<A extends Account> extends AbstractAccountConnection<A> {

	protected LoopedAbstractAccountConnection(@Nonnull A account, @Nonnull Context context, boolean internetConnectionRequired) {
		super(account, context, internetConnectionRequired);
	}

	@Override
	protected final void doWork() throws AccountConnectionException {
		// Try to create connection (if not exists)
		while (!isStopped()) {
			tryConnect();
			try {
				Thread.sleep(10L * 60L * 1000L);
			} catch (InterruptedException e) {
			}
		}
	}

	@Override
	protected final void stopWork() {
		disconnect();
	}

	protected abstract void disconnect();

	protected abstract void tryConnect() throws AccountConnectionException;
}
